package com.tinify;

/**
 * Media types that images can be converted to.
 *
 * <p>Use these constants with {@link Source#convert(Options)}:
 *
 * <pre>{@code
 * Tinify.fromFile("input.png")
 *       .convert(new Options().with("type", Format.JXL))
 *       .toFile("output.jxl");
 * }</pre>
 *
 * <p>Multiple types may be supplied, in which case the API returns the
 * smallest result:
 *
 * <pre>{@code
 * new Options().with("type", new String[]{Format.JXL, Format.WEBP});
 * }</pre>
 */
public final class Format {
    /** WebP, {@code image/webp}. */
    public static final String WEBP = "image/webp";

    /** PNG, {@code image/png}. */
    public static final String PNG = "image/png";

    /** JPEG, {@code image/jpeg}. */
    public static final String JPEG = "image/jpeg";

    /** JPEG, {@code image/jpg}. An alias of {@link #JPEG}. */
    public static final String JPG = "image/jpg";

    /** AVIF, {@code image/avif}. */
    public static final String AVIF = "image/avif";

    /** JPEG XL, {@code image/jxl}. */
    public static final String JXL = "image/jxl";

    /** Wildcard, {@code &#42;/&#42;}. Returns the smallest of the supported types. */
    public static final String ANY = "*/*";

    private Format() {
    }
}
