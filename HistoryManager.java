private void saveHistory() {
    SharedPreferences prefs = getSharedPreferences("url_history", MODE_PRIVATE);
    JSONArray arr = new JSONArray();

    for (String url : history) arr.put(url);

    prefs.edit().putString("history", arr.toString()).apply();
}
