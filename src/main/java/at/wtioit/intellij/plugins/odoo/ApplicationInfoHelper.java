package at.wtioit.intellij.plugins.odoo;

import com.intellij.openapi.application.ApplicationInfo;

/**
 * Helper that lets us define version dependent features.
 * Use with care. Only if JetBrains breaks backward compatibility.
 * And document why it is used.
 */
public interface ApplicationInfoHelper {

    enum Versions {
        V_2021("2021");

        private final int major;
        private final int minor;
        private final int bugfix;

        Versions(String fullVersionNumber) {
            String[] versionParts = fullVersionNumber.split("\\.");
            major = Integer.parseInt(versionParts[0]);
            if (versionParts.length > 1) {
                minor = Integer.parseInt(versionParts[1]);
            } else {
                minor = -1;
            }
            if (versionParts.length > 2) {
                bugfix = Integer.parseInt(versionParts[2]);
            } else {
                bugfix = -1;
            }

        }
    }

    static int getMajorVersion() {
        return Integer.parseInt(ApplicationInfo.getInstance().getMajorVersion());
    }

    static int getMinorVersion() {
        return Integer.parseInt(ApplicationInfo.getInstance().getMinorVersionMainPart());
    }

    static int getBugfixVersion() {
        String minorVersion = ApplicationInfo.getInstance().getMinorVersionMainPart();
        String fullMinorVersion = ApplicationInfo.getInstance().getMinorVersion();
        if (fullMinorVersion.length() > minorVersion.length() + 1) {
            return Integer.parseInt(fullMinorVersion.substring(minorVersion.length() + 1));
        } else {
            return -1;
        }

    }

    static boolean versionGreaterThanEqual(Versions version) {
        return getMajorVersion() >= version.major
                && getMinorVersion() >= version.minor
                && getBugfixVersion() >= version.bugfix;
    }
}
