/* Class22 - Decompiled by JODE
 * Visit http://jode.sourceforge.net/
 */

public abstract class Class22 {
    public static int anInt531;
    public static RSString aClass1_532;
    public static RSString aClass1_533;
    public static Buffer[] aClass40_Sub1Array534;
    public static IndexedColorSprite[] aClass40_Sub5_Sub14_Sub2Array535;
    public static int anInt536;
    public static int anInt537;
    public static RSString aClass1_538 = Class58.method978("Angreifen");
    public static int anInt539;
    public static int anInt540;
    public static boolean aBoolean541;
    public static RSString aClass1_542;
    public static int anInt543;
    public static boolean aBoolean544;
    public static int anInt545;
    public static RSString aClass1_546;
    public static int anInt547;
    public static boolean[] aBooleanArray548;

    static {
        anInt537 = 0;
        aClass40_Sub1Array534 = new Buffer[2048];
        aClass1_533 = Class58.method978("M");
        aClass1_542 = aClass1_533;
        aBoolean541 = false;
        aBoolean544 = false;
        anInt547 = 0;
        aClass1_546 = Class58.method978("Your account is already logged in)3");
        aClass1_532 = aClass1_546;
        aBooleanArray548 = new boolean[]{true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, false, true, false, false};
    }

    public static void method298(int arg0) {
        try {
            aClass1_546 = null;
            aClass1_532 = null;
            aClass1_538 = null;
            aClass40_Sub5_Sub14_Sub2Array535 = null;
            if(arg0 > 116) {
                aClass40_Sub1Array534 = null;
                aClass1_542 = null;
                aClass1_533 = null;
                aBooleanArray548 = null;
            }
        } catch(RuntimeException runtimeexception) {
            throw Class8.method216(runtimeexception, "hc.F(" + arg0 + ')');
        }
    }

    public static void method299(byte arg0, int arg1) {
        try {
            anInt543++;
            int[] is = (Class40_Sub5_Sub13.aClass40_Sub5_Sub14_Sub4_2765.anIntArray2955);
            int i = is.length;
            for(int i_0_ = 0; i > i_0_; i_0_++)
                is[i_0_] = 0;
            for(int i_1_ = 1; (i_1_ ^ 0xffffffff) > -104; i_1_++) {
                int i_2_ = 24628 - -((-(512 * i_1_) + 52736) * 4);
                for(int i_3_ = 1; (i_3_ ^ 0xffffffff) > -104; i_3_++) {
                    if((0x18 & (Floor.aByteArrayArrayArray2323[arg1][i_3_][i_1_])) == 0)
                        NPC.aClass2_3301.method96(is, i_2_, 512, arg1, i_3_, i_1_);
                    if((arg1 ^ 0xffffffff) > -4 && ((Floor.aByteArrayArrayArray2323[1 + arg1][i_3_][i_1_]) & 0x8 ^ 0xffffffff) != -1)
                        NPC.aClass2_3301.method96(is, i_2_, 512, 1 + arg1, i_3_, i_1_);
                    i_2_ += 4;
                }
            }
            Class40_Sub5_Sub13.aClass40_Sub5_Sub14_Sub4_2765.method723();
            int i_4_ = ((-10 + (int) (Math.random() * 20.0) + 238 << -838501336) + (228 - -(int) (Math.random() * 20.0) << 1375075344) + (238 - -(int) (20.0 * Math.random())) + -10);
            int i_5_ = -10 + ((int) (20.0 * Math.random()) + 238) << 2143047344;
            for(int i_6_ = 1; i_6_ < 103; i_6_++) {
                for(int i_7_ = 1; i_7_ < 103; i_7_++) {
                    if(((Floor.aByteArrayArrayArray2323[arg1][i_7_][i_6_]) & 0x18 ^ 0xffffffff) == -1)
                        Class40_Sub5_Sub17_Sub4.method781(1850, arg1, i_7_, i_4_, i_5_, i_6_);
                    if((arg1 ^ 0xffffffff) > -4 && (0x8 & (Floor.aByteArrayArrayArray2323[1 + arg1][i_7_][i_6_]) ^ 0xffffffff) != -1)
                        Class40_Sub5_Sub17_Sub4.method781(1850, 1 + arg1, i_7_, i_4_, i_5_, i_6_);
                }
            }
            Class40_Sub5_Sub17_Sub2.anInt3040 = 0;
            if(arg0 < 24)
                aClass40_Sub1Array534 = null;
            for(int i_8_ = 0; i_8_ < 104; i_8_++) {
                for(int i_9_ = 0; i_9_ < 104; i_9_++) {
                    int i_10_ = (NPC.aClass2_3301.method93(Player.anInt3267, i_8_, i_9_));
                    if(i_10_ != 0) {
                        i_10_ = 0x7fff & i_10_ >> -1684965490;
                        int i_11_ = (Class40_Sub4.method535(i_10_, (byte) 127).anInt2511);
                        if(i_11_ >= 0) {
                            int i_12_ = i_9_;
                            int i_13_ = i_8_;
                            if((i_11_ ^ 0xffffffff) != -23 && i_11_ != 29 && (i_11_ ^ 0xffffffff) != -35 && (i_11_ ^ 0xffffffff) != -37 && (i_11_ ^ 0xffffffff) != -47 && (i_11_ ^ 0xffffffff) != -48 && i_11_ != 48) {
                                int[][] is_14_ = (Landscape.aClass3Array1167[Player.anInt3267].anIntArrayArray150);
                                for(int i_15_ = 0; (i_15_ ^ 0xffffffff) > -11; i_15_++) {
                                    int i_16_ = (int) (Math.random() * 4.0);
                                    if(i_16_ == 0 && (i_13_ ^ 0xffffffff) < -1 && i_13_ > -3 + i_8_ && ((is_14_[-1 + i_13_][i_12_] & 0x1280108) ^ 0xffffffff) == -1)
                                        i_13_--;
                                    if((i_16_ ^ 0xffffffff) == -2 && i_13_ < 103 && i_13_ < i_8_ + 3 && (is_14_[i_13_ - -1][i_12_] & 0x1280180) == 0)
                                        i_13_++;
                                    if((i_16_ ^ 0xffffffff) == -3 && i_12_ > 0 && i_12_ > -3 + i_9_ && ((is_14_[i_13_][i_12_ - 1] & 0x1280102) ^ 0xffffffff) == -1)
                                        i_12_--;
                                    if(i_16_ == 3 && (i_12_ ^ 0xffffffff) > -104 && 3 + i_9_ > i_12_ && ((0x1280120 & is_14_[i_13_][1 + i_12_]) ^ 0xffffffff) == -1)
                                        i_12_++;
                                }
                            }
                            Class62.aClass40_Sub5_Sub14_Sub4Array1466[Class40_Sub5_Sub17_Sub2.anInt3040] = (Class8.aClass40_Sub5_Sub14_Sub4Array296[i_11_]);
                            Class40_Sub5_Sub17_Sub4.anIntArray3149[Class40_Sub5_Sub17_Sub2.anInt3040] = i_13_;
                            Class45.anIntArray1083[(Class40_Sub5_Sub17_Sub2.anInt3040)] = i_12_;
                            Class40_Sub5_Sub17_Sub2.anInt3040++;
                        }
                    }
                }
            }
        } catch(RuntimeException runtimeexception) {
            throw Class8.method216(runtimeexception, "hc.L(" + arg0 + ',' + arg1 + ')');
        }
    }

    public static void method305(byte arg0) {
        do {
            try {
                RSCanvas.aClass68_64.method1046((byte) 90);
                Class44.aClass40_Sub5_Sub14_Sub2_1047.method692(0, 0);
                anInt539++;
                Class5.anIntArray198 = R3D.method708(Class5.anIntArray198);
                if(arg0 > 2)
                    break;
                method305((byte) 88);
            } catch(RuntimeException runtimeexception) {
                throw Class8.method216(runtimeexception, "hc.C(" + arg0 + ')');
            }
            break;
        } while(false);
    }

    public static RSString method307(Buffer arg0, int arg1, int arg2) {
        try {
            anInt536++;
            try {
                if(arg1 != -1)
                    aBooleanArray548 = null;
                RSString class1 = new RSString();
                class1.anInt1680 = arg0.method502((byte) -81);
                if((arg2 ^ 0xffffffff) > (class1.anInt1680 ^ 0xffffffff))
                    class1.anInt1680 = arg2;
                class1.aByteArray1692 = new byte[class1.anInt1680];
                arg0.offset += (Class40_Sub5_Sub10.aClass66_2590.method1023(arg0.payload, class1.anInt1680, 0, class1.aByteArray1692, arg0.offset, -1));
                return class1;
            } catch(Exception exception) {
                return Class33.aClass1_777;
            }
        } catch(RuntimeException runtimeexception) {
            throw Class8.method216(runtimeexception, ("hc.G(" + (arg0 != null ? "{...}" : "null") + ',' + arg1 + ',' + arg2 + ')'));
        }
    }

    public static void method308(int arg0) {
        try {
            anInt540++;
            int i = -39 % ((arg0 - 33) / 58);
            RSString.method56(2037, false, null, 0);
        } catch(RuntimeException runtimeexception) {
            throw Class8.method216(runtimeexception, "hc.D(" + arg0 + ')');
        }
    }

    public static void method309(int arg0, int arg1) {
        do {
            try {
                Class40_Sub5_Sub7.anInt2480 = Node.anInt926;
                Class66.method1030((byte) 127);
                anInt531++;
                int i = (NPC.method795((byte) -70, arg1).anInt2633);
                if(i != 0) {
                    int i_21_ = Class58.varbitmasks[arg1];
                    if((i ^ 0xffffffff) == -2) {
                        if((i_21_ ^ 0xffffffff) == -2) {
                            R3D.method711(0.9);
                            ((Class35) R3D.anInterface3_2939).method424(108, 0.9);
                        }
                        if((i_21_ ^ 0xffffffff) == -3) {
                            R3D.method711(0.8);
                            ((Class35) R3D.anInterface3_2939).method424(97, 0.8);
                        }
                        if(i_21_ == 3) {
                            R3D.method711(0.7);
                            ((Class35) R3D.anInterface3_2939).method424(99, 0.7);
                        }
                        if((i_21_ ^ 0xffffffff) == -5) {
                            R3D.method711(0.6);
                            ((Class35) R3D.anInterface3_2939).method424(arg0 ^ ~0x6a, 0.6);
                        }
                        Class40_Sub5_Sub17_Sub2.method774((byte) -96);
                        Class40_Sub5_Sub11.aBoolean2631 = true;
                    }
                    if(i == 3) {
                        int i_22_ = 0;
                        if((i_21_ ^ 0xffffffff) == -1)
                            i_22_ = 255;
                        if(i_21_ == 1)
                            i_22_ = 192;
                        if((i_21_ ^ 0xffffffff) == -3)
                            i_22_ = 128;
                        if((i_21_ ^ 0xffffffff) == -4)
                            i_22_ = 64;
                        if(i_21_ == 4)
                            i_22_ = 0;
                        if(i_22_ != RSCanvas.anInt60) {
                            if(RSCanvas.anInt60 != 0 || Class62.anInt1457 == -1) {
                                if((i_22_ ^ 0xffffffff) == -1) {
                                    Class33.method402(false);
                                    Class35.anInt1733 = 0;
                                } else
                                    Node.method456(i_22_, (byte) 81);
                            } else {
                                Class33.method414(false, 0, Class62.anInt1457, i_22_, 0, Class55.aClass6_Sub1_1286, arg0);
                                Class35.anInt1733 = 0;
                            }
                            RSCanvas.anInt60 = i_22_;
                        }
                    }
                    if(i == 9)
                        Class43.anInt1025 = i_21_;
                    if(i == 10) {
                        if(i_21_ == 0)
                            RSCanvas.anInt65 = 127;
                        if(i_21_ == 1)
                            RSCanvas.anInt65 = 96;
                        if(i_21_ == 2)
                            RSCanvas.anInt65 = 64;
                        if((i_21_ ^ 0xffffffff) == -4)
                            RSCanvas.anInt65 = 32;
                        if((i_21_ ^ 0xffffffff) == -5)
                            RSCanvas.anInt65 = 0;
                    }
                    if(i == 8) {
                        Class52.aBoolean1221 = true;
                        Class3.anInt165 = i_21_;
                    }
                    if((i ^ 0xffffffff) == -5) {
                        if(i_21_ == 0)
                            Class5.anInt200 = 127;
                        if(i_21_ == 1)
                            Class5.anInt200 = 96;
                        if((i_21_ ^ 0xffffffff) == -3)
                            Class5.anInt200 = 64;
                        if((i_21_ ^ 0xffffffff) == -4)
                            Class5.anInt200 = 32;
                        if((i_21_ ^ 0xffffffff) == -5)
                            Class5.anInt200 = 0;
                    }
                    if((i ^ 0xffffffff) == -7)
                        Class40_Sub5_Sub1.anInt2280 = i_21_;
                    if(arg0 != -1)
                        method309(74, -85);
                    if((i ^ 0xffffffff) != -6)
                        break;
                    Class68.anInt1630 = i_21_;
                }
            } catch(RuntimeException runtimeexception) {
                throw Class8.method216(runtimeexception, "hc.A(" + arg0 + ',' + arg1 + ')');
            }
            break;
        } while(false);
    }

    public abstract void method300(byte[] is, boolean bool, int i, int i_17_);

    public abstract void method301(int i, int i_18_);

    public abstract void method302(int i);

    public abstract void method303(byte i);

    public abstract void method304(byte i, int i_19_, int i_20_);

    public abstract void method306(byte i);
}
