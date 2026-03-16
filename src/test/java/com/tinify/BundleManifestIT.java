package com.tinify;

import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.jar.Attributes;

import static org.junit.Assert.*;

public class BundleManifestIT {

    private static final List<String> FORBIDDEN_IMPORTS = Arrays.asList(
        "org.openjsse.",
        "dalvik.",
        "android.",
        "kotlin.reflect.jvm.internal"
    );

    private Manifest loadBundleManifest() throws IOException {
        String buildDir = System.getProperty("project.build.directory", "target");
        String finalName = System.getProperty("project.build.finalName", "tinify-1.8.8");
        File jar = new File(buildDir, finalName + ".jar");
        assertTrue("Bundle JAR not found: " + jar.getAbsolutePath(), jar.exists());
        try (JarFile jarFile = new JarFile(jar)) {
            return jarFile.getManifest();
        }
    }

    @Test
    public void bundleHasRequiredOsgiHeaders() throws IOException {
        Attributes attrs = loadBundleManifest().getMainAttributes();

        assertEquals("2", attrs.getValue("Bundle-ManifestVersion"));
        assertNotNull("Missing Bundle-SymbolicName", attrs.getValue("Bundle-SymbolicName"));
        assertNotNull("Missing Bundle-Version", attrs.getValue("Bundle-Version"));
        assertNotNull("Missing Export-Package", attrs.getValue("Export-Package"));
    }

    @Test
    public void importPackageDoesNotContainUnresolvablePackages() throws IOException {
        Attributes attrs = loadBundleManifest().getMainAttributes();
        String importPackage = attrs.getValue("Import-Package");
        assertNotNull("Missing Import-Package header", importPackage);

        for (String forbidden : FORBIDDEN_IMPORTS) {
            assertFalse(
                "Import-Package must not contain " + forbidden + " but was: " + importPackage,
                importPackage.contains(forbidden)
            );
        }
    }

    @Test
    public void exportPackageContainsTinify() throws IOException {
        Attributes attrs = loadBundleManifest().getMainAttributes();
        String exportPackage = attrs.getValue("Export-Package");
        assertTrue(
            "Export-Package must contain com.tinify",
            exportPackage.contains("com.tinify")
        );
    }

    @Test
    public void bundleEmbedsDependencies() throws IOException {
        Attributes attrs = loadBundleManifest().getMainAttributes();
        String bundleClassPath = attrs.getValue("Bundle-ClassPath");
        assertNotNull("Missing Bundle-ClassPath", bundleClassPath);
        assertTrue("Bundle-ClassPath must contain okhttp", bundleClassPath.contains("okhttp"));
        assertTrue("Bundle-ClassPath must contain gson", bundleClassPath.contains("gson"));
        assertTrue("Bundle-ClassPath must contain okio", bundleClassPath.contains("okio"));
    }
}
