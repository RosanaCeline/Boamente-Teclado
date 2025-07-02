package com.anysoftkeyboard.releaseinfo;

import android.net.Uri;
import android.text.TextUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class VersionChangeLogs {
  static List<VersionChangeLog> createChangeLog() {
    final List<VersionChangeLog> log = new ArrayList<>();

    log.add(
        new VersionChangeLog(
            1,
            12,
            "",
            Uri.parse("https://github.com/AnySoftKeyboard/AnySoftKeyboard/milestone/94"),
            "Support for Android 14.",
            "Deseja",
            "Several fixes to the settings app navigation.",
            "Vibration fixes.",
            "Suggestions pick and order fixes.",
            "Improved pop-up keys order.",
            "Gesture-typing supports user-dictionary.",
            "Support for direct-boot devices.",
            "Reduced installation size (for supporting devices).",
            "Updated translations from the community (at crowdin.net)."));
    return log;
  }

  public static class VersionChangeLog {
    public final String versionName;
    public final String[] changes;
    public final Uri changesWebUrl;

    public VersionChangeLog(
        int major, int minor, String qualifier, Uri changesWebUrl, String... changes) {
      if (TextUtils.isEmpty(qualifier)) {
        this.versionName = String.format(Locale.US, "%d.%d", major, minor);
      } else {
        this.versionName = String.format(Locale.US, "%d.%d-%s", major, minor, qualifier);
      }
      this.changes = changes;
      this.changesWebUrl = changesWebUrl;
    }
  }
}
