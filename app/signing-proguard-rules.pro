# BouncyCastleProvider registers JCA services by class-name strings and loads
# algorithm Mappings via reflection. Keep those entry points and SPI members;
# implementation classes outside this package can still be optimized normally.
-keep class org.bouncycastle.jcajce.provider.** {
    public protected *;
}
