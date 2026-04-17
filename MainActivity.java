// ※重要部分のみ抜粋（長すぎるため整理版）

// フィールド
private List<String> history = new ArrayList<>();
private Set<String> detectedUrls = new HashSet<>();
private List<Rect> detectedRects = new ArrayList<>();

private UrlAdapter adapter;
private OverlayView overlayView;
private PreviewView previewView;

private SharedPreferences prefs;

// onCreate
prefs = getSharedPreferences("settings", MODE_PRIVATE);

// RecyclerView
adapter = new UrlAdapter(this, history);
recyclerView.setAdapter(adapter);

// OCR検出時
private void handleDetectedUrl(String url) {

    long now = System.currentTimeMillis();

    if (!detectedUrls.contains(url)) {
        detectedUrls.add(url);

        // 履歴追加
        if (!history.contains(url)) {
            history.add(0, url);

            int max = prefs.getInt("history_size", 30);
            if (history.size() > max) {
                history.remove(history.size() - 1);
            }

            adapter.notifyItemInserted(0);
            saveHistory();
        }

        // 自動で開く
        if (prefs.getBoolean("auto_open", false)) {
            String fixed = url.startsWith("http") ? url : "https://" + url;
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(fixed)));
        }

        showResult(url);
    }
}
