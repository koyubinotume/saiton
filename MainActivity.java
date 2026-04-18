package com.example.urlcamera;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.widget.FrameLayout;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.*;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;

import org.json.JSONArray;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {

    private PreviewView previewView;
    private OverlayView overlayView;
    private RecyclerView recyclerView;

    private UrlAdapter adapter;

    private List<String> history = new ArrayList<>();
    private Set<String> detectedUrls = new HashSet<>();
    private List<Rect> detectedRects = new ArrayList<>();

    private TextRecognizer recognizer;

    private long lastProcessTime = 0;
    private long lastOpenTime = 0;

    private SharedPreferences prefs;

    private final Pattern urlPattern = Pattern.compile(
            "(https?://[\\w\\-._~:/?#[\\]@!$&'()*+,;=%]+)|(www\\.[\\w\\-._~:/?#[\\]@!$&'()*+,;=%]+)"
    );

    // 権限リクエスト
    private final ActivityResultLauncher<String> permissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), granted -> {
                if (granted) startCamera();
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        previewView = new PreviewView(this);
        overlayView = new OverlayView(this);

        recyclerView = new RecyclerView(this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        FrameLayout layout = new FrameLayout(this);
        layout.addView(previewView);
        layout.addView(overlayView);
        layout.addView(recyclerView);

        setContentView(layout);

        prefs = getSharedPreferences("settings", MODE_PRIVATE);

        loadHistory();

        adapter = new UrlAdapter(this, history);
        recyclerView.setAdapter(adapter);

        recognizer = TextRecognition.getClient();

        checkPermission();
    }

    private void checkPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {

            permissionLauncher.launch(Manifest.permission.CAMERA);
        } else {
            startCamera();
        }
    }

    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> future =
                ProcessCameraProvider.getInstance(this);

        future.addListener(() -> {
            try {
                ProcessCameraProvider provider = future.get();

                Preview preview = new Preview.Builder().build();
                preview.setSurfaceProvider(previewView.getSurfaceProvider());

                ImageAnalysis analysis = new ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build();

                analysis.setAnalyzer(ContextCompat.getMainExecutor(this), this::processImage);

                provider.unbindAll();
                provider.bindToLifecycle(
                        this,
                        CameraSelector.DEFAULT_BACK_CAMERA,
                        preview,
                        analysis
                );

            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private void processImage(ImageProxy imageProxy) {

        // 負荷軽減（0.5秒間隔）
        long now = System.currentTimeMillis();
        if (now - lastProcessTime < 500) {
            imageProxy.close();
            return;
        }
        lastProcessTime = now;

        if (imageProxy.getImage() == null) {
            imageProxy.close();
            return;
        }

        InputImage image = InputImage.fromMediaImage(
                imageProxy.getImage(),
                imageProxy.getImageInfo().getRotationDegrees()
        );

        recognizer.process(image)
                .addOnSuccessListener(visionText -> {

                    detectedRects.clear();

                    for (Text.TextBlock block : visionText.getTextBlocks()) {
                        for (Text.Line line : block.getLines()) {

                            Matcher matcher = urlPattern.matcher(line.getText());

                            while (matcher.find()) {
                                String url = matcher.group();
                                handleDetectedUrl(url);
                            }
                        }
                    }

                })
                .addOnFailureListener(e -> {
                })
                .addOnCompleteListener(task -> imageProxy.close());
    }

    private void handleDetectedUrl(String url) {

        long now = System.currentTimeMillis();

        // 重複防止
        if (detectedUrls.contains(url)) return;

        detectedUrls.add(url);

        // 履歴追加
        if (!history.contains(url)) {
            history.add(0, url);

            int max = prefs.getInt("history_size", 30);
            if (history.size() > max) {
                history.remove(history.size() - 1);
            }

            adapter.notifyDataSetChanged();
            saveHistory();
        }

        // 自動オープン（クールダウン付き）
        if (prefs.getBoolean("auto_open", false) && now - lastOpenTime > 3000) {

            lastOpenTime = now;

            String fixed = url.startsWith("http") ? url : "https://" + url;
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(fixed)));
        }

        // メモリ肥大防止
        if (detectedUrls.size() > 50) {
            detectedUrls.clear();
        }
    }

    private void saveHistory() {
        SharedPreferences prefs = getSharedPreferences("url_history", MODE_PRIVATE);

        JSONArray arr = new JSONArray();
        for (String url : history) arr.put(url);

        prefs.edit().putString("history", arr.toString()).apply();
    }

    private void loadHistory() {
        SharedPreferences prefs = getSharedPreferences("url_history", MODE_PRIVATE);
        String json = prefs.getString("history", null);

        if (json == null) return;

        try {
            JSONArray arr = new JSONArray(json);
            for (int i = 0; i < arr.length(); i++) {
                history.add(arr.getString(i));
            }
        } catch (Exception e) {
            history.clear();
        }
    }
}
