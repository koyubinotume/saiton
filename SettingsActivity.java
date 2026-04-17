public class SettingsActivity extends AppCompatActivity {

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        SharedPreferences prefs = getSharedPreferences("settings", MODE_PRIVATE);

        Switch dark = findViewById(R.id.switchDark);
        Switch auto = findViewById(R.id.switchAutoOpen);

        dark.setChecked(prefs.getBoolean("dark", false));
        auto.setChecked(prefs.getBoolean("auto_open", false));

        dark.setOnCheckedChangeListener((b, v) -> {
            prefs.edit().putBoolean("dark", v).apply();
            AppCompatDelegate.setDefaultNightMode(
                v ? AppCompatDelegate.MODE_NIGHT_YES
                  : AppCompatDelegate.MODE_NIGHT_NO
            );
        });

        auto.setOnCheckedChangeListener((b, v) ->
            prefs.edit().putBoolean("auto_open", v).apply()
        );
    }
}
