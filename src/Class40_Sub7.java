/* Class40_Sub7 - Decompiled by JODE
 * Visit http://jode.sourceforge.net/
 */

public class Class40_Sub7 extends Node {
    public static int anInt2122;
    public static Class68 aClass68_2123;
    public static RSString aClass1_2125;
    public static int anInt2126;
    public static RSString aClass1_2127 = Class58.method978("Loading interfaces )2 ");
    public static RSString aClass1_2128 = Class58.method978("Fertigkeit)2");
    public static RSString aClass1_2129 = Class58.method978("Mem:");
    public static RSString aClass1_2130 = Class58.method978("Loaded update list");
    public static int[] anIntArray2131;
    public static RSString aClass1_2132;

    static {
        aClass1_2125 = aClass1_2130;
        anIntArray2131 = new int[200];
        aClass1_2132 = aClass1_2127;
    }

    public RSString aClass1_2124;

    public static void method839(int arg0) {
        try {
            aClass68_2123 = null;
            aClass1_2132 = null;
            anIntArray2131 = null;
            aClass1_2128 = null;
            aClass1_2129 = null;
            if(arg0 == 63) {
                aClass1_2127 = null;
                aClass1_2130 = null;
                aClass1_2125 = null;
            }
        } catch(RuntimeException runtimeexception) {
            throw Class8.method216(runtimeexception, "ma.A(" + arg0 + ')');
        }
    }

    public static boolean method840(byte arg0, byte[] arg1, int arg2, int arg3) {
        try {
            anInt2126++;
            boolean bool = true;
            Buffer class40_sub1 = new Buffer(arg1);
            int i = -1;
            if(arg0 > -40)
                return false;
            for(; ; ) {
                int i_0_ = class40_sub1.method502((byte) -94);
                if(i_0_ == 0)
                    break;
                i += i_0_;
                int i_1_ = 0;
                boolean bool_2_ = false;
                for(; ; ) {
                    if(bool_2_) {
                        int i_3_ = class40_sub1.method502((byte) -84);
                        if(i_3_ == 0)
                            break;
                        class40_sub1.method468(false);
                    } else {
                        int i_4_ = class40_sub1.method502((byte) -114);
                        if((i_4_ ^ 0xffffffff) == -1)
                            break;
                        i_1_ += i_4_ + -1;
                        int i_5_ = i_1_ & 0x3f;
                        int i_6_ = class40_sub1.method468(false) >> -314035070;
                        int i_7_ = 0x3f & i_1_ >> -1291824762;
                        int i_8_ = i_7_ - -arg2;
                        int i_9_ = i_5_ - -arg3;
                        if((i_8_ ^ 0xffffffff) < -1 && i_9_ > 0 && i_8_ < 103 && (i_9_ ^ 0xffffffff) > -104) {
                            Class40_Sub5_Sub8 class40_sub5_sub8 = Class40_Sub4.method535(i, (byte) 127);
                            if((i_6_ ^ 0xffffffff) != -23 || !Class46.aBoolean1112 || ((class40_sub5_sub8.anInt2546 ^ 0xffffffff) != -1) || class40_sub5_sub8.aBoolean2547) {
                                bool_2_ = true;
                                if(!class40_sub5_sub8.method612((byte) 8)) {
                                    bool = false;
                                    Class40_Sub5_Sub10.anInt2591++;
                                }
                            }
                        }
                    }
                }
            }
            return bool;
        } catch(RuntimeException runtimeexception) {
            throw Class8.method216(runtimeexception, ("ma.B(" + arg0 + ',' + (arg1 != null ? "{...}" : "null") + ',' + arg2 + ',' + arg3 + ')'));
        }
    }
}
