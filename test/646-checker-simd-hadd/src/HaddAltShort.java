/*
 * Copyright (C) 2017 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 * Tests for halving-add idiomatic vectorization.
 *
 * Alternative version expressed with logical shift right
 * in the higher precision (has no impact on idiom).
 */
public class HaddAltShort {

  private static final int N = 64 * 1024;
  private static final int M = N + 31;

  static short[] sB1 = new short[M];
  static short[] sB2 = new short[M];
  static short[] sBo = new short[M];

  /// CHECK-START: void HaddAltShort.halving_add_signed(short[], short[], short[]) loop_optimization (before)
  /// CHECK-DAG: <<I1:i\d+>>   IntConstant 1                       loop:none
  /// CHECK-DAG: <<Phi:i\d+>>  Phi                                 loop:<<Loop:B\d+>> outer_loop:none
  /// CHECK-DAG: <<Get1:s\d+>> ArrayGet                            loop:<<Loop>>      outer_loop:none
  /// CHECK-DAG: <<Get2:s\d+>> ArrayGet                            loop:<<Loop>>      outer_loop:none
  /// CHECK-DAG: <<Add:i\d+>>  Add [<<Get1>>,<<Get2>>]             loop:<<Loop>>      outer_loop:none
  /// CHECK-DAG: <<UShr:i\d+>> UShr [<<Add>>,<<I1>>]               loop:<<Loop>>      outer_loop:none
  /// CHECK-DAG: <<Cnv:s\d+>>  TypeConversion [<<UShr>>]           loop:<<Loop>>      outer_loop:none
  /// CHECK-DAG:               ArraySet [{{l\d+}},<<Phi>>,<<Cnv>>] loop:<<Loop>>      outer_loop:none
  //
  /// CHECK-START-ARM: void HaddAltShort.halving_add_signed(short[], short[], short[]) loop_optimization (after)
  /// CHECK-DAG: <<Get1:d\d+>> VecLoad                               loop:<<Loop:B\d+>> outer_loop:none
  /// CHECK-DAG: <<Get2:d\d+>> VecLoad                               loop:<<Loop>>      outer_loop:none
  /// CHECK-DAG: <<HAdd:d\d+>> VecHalvingAdd [<<Get1>>,<<Get2>>] packed_type:Int16 rounded:false loop:<<Loop>> outer_loop:none
  /// CHECK-DAG:               VecStore [{{l\d+}},{{i\d+}},<<HAdd>>] loop:<<Loop>>      outer_loop:none
  //
  /// CHECK-START-ARM64: void HaddAltShort.halving_add_signed(short[], short[], short[]) loop_optimization (after)
  /// CHECK-IF:     hasIsaFeature("sve") and os.environ.get('ART_FORCE_TRY_PREDICATED_SIMD') == 'true'
  //
  //      HalvingAdd idiom is not supported for SVE.
  ///     CHECK-NOT: VecHalvingAdd
  //
  /// CHECK-ELSE:
  //
  ///     CHECK-DAG: <<Get1:d\d+>> VecLoad                               loop:<<Loop:B\d+>> outer_loop:none
  ///     CHECK-DAG: <<Get2:d\d+>> VecLoad                               loop:<<Loop>>      outer_loop:none
  ///     CHECK-DAG: <<HAdd:d\d+>> VecHalvingAdd [<<Get1>>,<<Get2>>] packed_type:Int16 rounded:false loop:<<Loop>> outer_loop:none
  ///     CHECK-DAG:               VecStore [{{l\d+}},{{i\d+}},<<HAdd>>] loop:<<Loop>>      outer_loop:none
  //
  /// CHECK-FI:
  private static void halving_add_signed(short[] b1, short[] b2, short[] bo) {
    int min_length = Math.min(bo.length, Math.min(b1.length, b2.length));
    for (int i = 0; i < min_length; i++) {
      bo[i] = (short) ((b1[i] + b2[i]) >>> 1);
    }
  }

  /// CHECK-START: void HaddAltShort.halving_add_unsigned(short[], short[], short[]) instruction_simplifier (before)
  /// CHECK-DAG: <<I1:i\d+>>   IntConstant 1                       loop:none
  /// CHECK-DAG: <<UMAX:i\d+>> IntConstant 65535                   loop:none
  /// CHECK-DAG: <<Phi:i\d+>>  Phi                                 loop:<<Loop:B\d+>> outer_loop:none
  /// CHECK-DAG: <<Get1:s\d+>> ArrayGet                            loop:<<Loop>>      outer_loop:none
  /// CHECK-DAG: <<Get2:s\d+>> ArrayGet                            loop:<<Loop>>      outer_loop:none
  /// CHECK-DAG: <<And1:i\d+>> And [<<Get1>>,<<UMAX>>]             loop:<<Loop>>      outer_loop:none
  /// CHECK-DAG: <<And2:i\d+>> And [<<UMAX>>,<<Get2>>]             loop:<<Loop>>      outer_loop:none
  /// CHECK-DAG: <<Add:i\d+>>  Add [<<And1>>,<<And2>>]             loop:<<Loop>>      outer_loop:none
  /// CHECK-DAG: <<UShr:i\d+>> UShr [<<Add>>,<<I1>>]               loop:<<Loop>>      outer_loop:none
  /// CHECK-DAG: <<Cnv:s\d+>>  TypeConversion [<<UShr>>]           loop:<<Loop>>      outer_loop:none
  /// CHECK-DAG:               ArraySet [{{l\d+}},{{i\d+}},<<Cnv>>] loop:<<Loop>>      outer_loop:none
  //
  /// CHECK-START: void HaddAltShort.halving_add_unsigned(short[], short[], short[]) loop_optimization (before)
  /// CHECK-DAG: <<I1:i\d+>>   IntConstant 1                       loop:none
  /// CHECK-DAG: <<Phi:i\d+>>  Phi                                 loop:<<Loop:B\d+>> outer_loop:none
  /// CHECK-DAG: <<Get1:c\d+>> ArrayGet                            loop:<<Loop>>      outer_loop:none
  /// CHECK-DAG: <<Get2:c\d+>> ArrayGet                            loop:<<Loop>>      outer_loop:none
  /// CHECK-DAG: <<Add:i\d+>>  Add [<<Get1>>,<<Get2>>]             loop:<<Loop>>      outer_loop:none
  /// CHECK-DAG: <<UShr:i\d+>> UShr [<<Add>>,<<I1>>]               loop:<<Loop>>      outer_loop:none
  /// CHECK-DAG: <<Cnv:s\d+>>  TypeConversion [<<UShr>>]           loop:<<Loop>>      outer_loop:none
  /// CHECK-DAG:               ArraySet [{{l\d+}},<<Phi>>,<<Cnv>>] loop:<<Loop>>      outer_loop:none
  //
  /// CHECK-START-ARM: void HaddAltShort.halving_add_unsigned(short[], short[], short[]) loop_optimization (after)
  /// CHECK-DAG: <<Get1:d\d+>> VecLoad                               loop:<<Loop:B\d+>> outer_loop:none
  /// CHECK-DAG: <<Get2:d\d+>> VecLoad                               loop:<<Loop>>      outer_loop:none
  /// CHECK-DAG: <<HAdd:d\d+>> VecHalvingAdd [<<Get1>>,<<Get2>>] packed_type:Uint16 rounded:false loop:<<Loop>> outer_loop:none
  /// CHECK-DAG:               VecStore [{{l\d+}},{{i\d+}},<<HAdd>>] loop:<<Loop>>      outer_loop:none
  //
  /// CHECK-START-ARM64: void HaddAltShort.halving_add_unsigned(short[], short[], short[]) loop_optimization (after)
  /// CHECK-IF:     hasIsaFeature("sve") and os.environ.get('ART_FORCE_TRY_PREDICATED_SIMD') == 'true'
  //
  //      HalvingAdd idiom is not supported for SVE.
  ///     CHECK-NOT: VecHalvingAdd
  //
  /// CHECK-ELSE:
  //
  ///     CHECK-DAG: <<Get1:d\d+>> VecLoad                               loop:<<Loop:B\d+>> outer_loop:none
  ///     CHECK-DAG: <<Get2:d\d+>> VecLoad                               loop:<<Loop>>      outer_loop:none
  ///     CHECK-DAG: <<HAdd:d\d+>> VecHalvingAdd [<<Get1>>,<<Get2>>] packed_type:Uint16 rounded:false loop:<<Loop>> outer_loop:none
  ///     CHECK-DAG:               VecStore [{{l\d+}},{{i\d+}},<<HAdd>>] loop:<<Loop>>      outer_loop:none
  //
  /// CHECK-FI:
  private static void halving_add_unsigned(short[] b1, short[] b2, short[] bo) {
    int min_length = Math.min(bo.length, Math.min(b1.length, b2.length));
    for (int i = 0; i < min_length; i++) {
      int v1 = b1[i] & 0xffff;
      int v2 = b2[i] & 0xffff;
      bo[i] = (short) ((v1 + v2) >>> 1);
    }
  }

  /// CHECK-START: void HaddAltShort.rounding_halving_add_signed(short[], short[], short[]) loop_optimization (before)
  /// CHECK-DAG: <<I1:i\d+>>   IntConstant 1                       loop:none
  /// CHECK-DAG: <<Phi:i\d+>>  Phi                                 loop:<<Loop:B\d+>> outer_loop:none
  /// CHECK-DAG: <<Get1:s\d+>> ArrayGet                            loop:<<Loop>>      outer_loop:none
  /// CHECK-DAG: <<Get2:s\d+>> ArrayGet                            loop:<<Loop>>      outer_loop:none
  /// CHECK-DAG: <<Add1:i\d+>> Add [<<Get1>>,<<Get2>>]             loop:<<Loop>>      outer_loop:none
  /// CHECK-DAG: <<Add2:i\d+>> Add [<<Add1>>,<<I1>>]               loop:<<Loop>>      outer_loop:none
  /// CHECK-DAG: <<UShr:i\d+>> UShr [<<Add2>>,<<I1>>]              loop:<<Loop>>      outer_loop:none
  /// CHECK-DAG: <<Cnv:s\d+>>  TypeConversion [<<UShr>>]           loop:<<Loop>>      outer_loop:none
  /// CHECK-DAG:               ArraySet [{{l\d+}},<<Phi>>,<<Cnv>>] loop:<<Loop>>      outer_loop:none
  //
  /// CHECK-START-ARM: void HaddAltShort.rounding_halving_add_signed(short[], short[], short[]) loop_optimization (after)
  /// CHECK-DAG: <<Get1:d\d+>> VecLoad                               loop:<<Loop:B\d+>> outer_loop:none
  /// CHECK-DAG: <<Get2:d\d+>> VecLoad                               loop:<<Loop>>      outer_loop:none
  /// CHECK-DAG: <<HAdd:d\d+>> VecHalvingAdd [<<Get1>>,<<Get2>>] packed_type:Int16 rounded:true loop:<<Loop>> outer_loop:none
  /// CHECK-DAG:               VecStore [{{l\d+}},{{i\d+}},<<HAdd>>] loop:<<Loop>>      outer_loop:none
  //
  /// CHECK-START-ARM64: void HaddAltShort.rounding_halving_add_signed(short[], short[], short[]) loop_optimization (after)
  /// CHECK-IF:     hasIsaFeature("sve") and os.environ.get('ART_FORCE_TRY_PREDICATED_SIMD') == 'true'
  //
  //      HalvingAdd idiom is not supported for SVE.
  ///     CHECK-NOT: VecHalvingAdd
  //
  /// CHECK-ELSE:
  //
  ///     CHECK-DAG: <<Get1:d\d+>> VecLoad                               loop:<<Loop:B\d+>> outer_loop:none
  ///     CHECK-DAG: <<Get2:d\d+>> VecLoad                               loop:<<Loop>>      outer_loop:none
  ///     CHECK-DAG: <<HAdd:d\d+>> VecHalvingAdd [<<Get1>>,<<Get2>>] packed_type:Int16 rounded:true loop:<<Loop>> outer_loop:none
  ///     CHECK-DAG:               VecStore [{{l\d+}},{{i\d+}},<<HAdd>>] loop:<<Loop>>      outer_loop:none
  //
  /// CHECK-FI:
  private static void rounding_halving_add_signed(short[] b1, short[] b2, short[] bo) {
    int min_length = Math.min(bo.length, Math.min(b1.length, b2.length));
    for (int i = 0; i < min_length; i++) {
      bo[i] = (short) ((b1[i] + b2[i] + 1) >>> 1);
    }
  }

  /// CHECK-START: void HaddAltShort.rounding_halving_add_unsigned(short[], short[], short[]) instruction_simplifier (before)
  /// CHECK-DAG: <<I1:i\d+>>   IntConstant 1                       loop:none
  /// CHECK-DAG: <<UMAX:i\d+>> IntConstant 65535                   loop:none
  /// CHECK-DAG: <<Phi:i\d+>>  Phi                                 loop:<<Loop:B\d+>> outer_loop:none
  /// CHECK-DAG: <<Get1:s\d+>> ArrayGet                            loop:<<Loop>>      outer_loop:none
  /// CHECK-DAG: <<Get2:s\d+>> ArrayGet                            loop:<<Loop>>      outer_loop:none
  /// CHECK-DAG: <<And1:i\d+>> And [<<Get1>>,<<UMAX>>]             loop:<<Loop>>      outer_loop:none
  /// CHECK-DAG: <<And2:i\d+>> And [<<UMAX>>,<<Get2>>]             loop:<<Loop>>      outer_loop:none
  /// CHECK-DAG: <<Add1:i\d+>> Add [<<And1>>,<<And2>>]             loop:<<Loop>>      outer_loop:none
  /// CHECK-DAG: <<Add2:i\d+>> Add [<<Add1>>,<<I1>>]               loop:<<Loop>>      outer_loop:none
  /// CHECK-DAG: <<UShr:i\d+>> UShr [<<Add2>>,<<I1>>]              loop:<<Loop>>      outer_loop:none
  /// CHECK-DAG: <<Cnv:s\d+>>  TypeConversion [<<UShr>>]           loop:<<Loop>>      outer_loop:none
  /// CHECK-DAG:               ArraySet [{{l\d+}},{{i\d+}},<<Cnv>>] loop:<<Loop>>      outer_loop:none
  //
  /// CHECK-START: void HaddAltShort.rounding_halving_add_unsigned(short[], short[], short[]) loop_optimization (before)
  /// CHECK-DAG: <<I1:i\d+>>   IntConstant 1                       loop:none
  /// CHECK-DAG: <<Phi:i\d+>>  Phi                                 loop:<<Loop:B\d+>> outer_loop:none
  /// CHECK-DAG: <<Get1:c\d+>> ArrayGet                            loop:<<Loop>>      outer_loop:none
  /// CHECK-DAG: <<Get2:c\d+>> ArrayGet                            loop:<<Loop>>      outer_loop:none
  /// CHECK-DAG: <<Add1:i\d+>> Add [<<Get1>>,<<Get2>>]             loop:<<Loop>>      outer_loop:none
  /// CHECK-DAG: <<Add2:i\d+>> Add [<<Add1>>,<<I1>>]               loop:<<Loop>>      outer_loop:none
  /// CHECK-DAG: <<UShr:i\d+>> UShr [<<Add2>>,<<I1>>]              loop:<<Loop>>      outer_loop:none
  /// CHECK-DAG: <<Cnv:s\d+>>  TypeConversion [<<UShr>>]           loop:<<Loop>>      outer_loop:none
  /// CHECK-DAG:               ArraySet [{{l\d+}},<<Phi>>,<<Cnv>>] loop:<<Loop>>      outer_loop:none
  //
  /// CHECK-START-ARM: void HaddAltShort.rounding_halving_add_unsigned(short[], short[], short[]) loop_optimization (after)
  /// CHECK-DAG: <<Get1:d\d+>> VecLoad                               loop:<<Loop:B\d+>> outer_loop:none
  /// CHECK-DAG: <<Get2:d\d+>> VecLoad                               loop:<<Loop>>      outer_loop:none
  /// CHECK-DAG: <<HAdd:d\d+>> VecHalvingAdd [<<Get1>>,<<Get2>>] packed_type:Uint16 rounded:true loop:<<Loop>> outer_loop:none
  /// CHECK-DAG:               VecStore [{{l\d+}},{{i\d+}},<<HAdd>>] loop:<<Loop>>      outer_loop:none
  //
  /// CHECK-START-ARM64: void HaddAltShort.rounding_halving_add_unsigned(short[], short[], short[]) loop_optimization (after)
  /// CHECK-IF:     hasIsaFeature("sve") and os.environ.get('ART_FORCE_TRY_PREDICATED_SIMD') == 'true'
  //
  //      HalvingAdd idiom is not supported for SVE.
  ///     CHECK-NOT: VecHalvingAdd
  //
  /// CHECK-ELSE:
  //
  ///     CHECK-DAG: <<Get1:d\d+>> VecLoad                               loop:<<Loop:B\d+>> outer_loop:none
  ///     CHECK-DAG: <<Get2:d\d+>> VecLoad                               loop:<<Loop>>      outer_loop:none
  ///     CHECK-DAG: <<HAdd:d\d+>> VecHalvingAdd [<<Get1>>,<<Get2>>] packed_type:Uint16 rounded:true loop:<<Loop>> outer_loop:none
  ///     CHECK-DAG:               VecStore [{{l\d+}},{{i\d+}},<<HAdd>>] loop:<<Loop>>      outer_loop:none
  //
  /// CHECK-FI:
  private static void rounding_halving_add_unsigned(short[] b1, short[] b2, short[] bo) {
    int min_length = Math.min(bo.length, Math.min(b1.length, b2.length));
    for (int i = 0; i < min_length; i++) {
      int v1 = b1[i] & 0xffff;
      int v2 = b2[i] & 0xffff;
      bo[i] = (short) ((v1 + v2 + 1) >>> 1);
    }
  }

  /// CHECK-START: void HaddAltShort.halving_add_signed_constant(short[], short[]) loop_optimization (before)
  /// CHECK-DAG: <<I1:i\d+>>   IntConstant 1                       loop:none
  /// CHECK-DAG: <<SMAX:i\d+>> IntConstant 32767                   loop:none
  /// CHECK-DAG: <<Phi:i\d+>>  Phi                                 loop:<<Loop:B\d+>> outer_loop:none
  /// CHECK-DAG: <<Get:s\d+>>  ArrayGet                            loop:<<Loop>>      outer_loop:none
  /// CHECK-DAG: <<Add:i\d+>>  Add [<<Get>>,<<SMAX>>]              loop:<<Loop>>      outer_loop:none
  /// CHECK-DAG: <<UShr:i\d+>> UShr [<<Add>>,<<I1>>]               loop:<<Loop>>      outer_loop:none
  /// CHECK-DAG: <<Cnv:s\d+>>  TypeConversion [<<UShr>>]           loop:<<Loop>>      outer_loop:none
  /// CHECK-DAG:               ArraySet [{{l\d+}},<<Phi>>,<<Cnv>>] loop:<<Loop>>      outer_loop:none
  //
  /// CHECK-START-ARM: void HaddAltShort.halving_add_signed_constant(short[], short[]) loop_optimization (after)
  /// CHECK-DAG: <<SMAX:i\d+>> IntConstant 32767                     loop:none
  /// CHECK-DAG: <<Repl:d\d+>> VecReplicateScalar [<<SMAX>>]         loop:none
  /// CHECK-DAG: <<Get:d\d+>>  VecLoad                               loop:<<Loop:B\d+>> outer_loop:none
  /// CHECK-DAG: <<HAdd:d\d+>> VecHalvingAdd [<<Get>>,<<Repl>>] packed_type:Int16 rounded:false loop:<<Loop>> outer_loop:none
  /// CHECK-DAG:               VecStore [{{l\d+}},{{i\d+}},<<HAdd>>] loop:<<Loop>>      outer_loop:none
  //
  /// CHECK-START-ARM64: void HaddAltShort.halving_add_signed_constant(short[], short[]) loop_optimization (after)
  /// CHECK-IF:     hasIsaFeature("sve") and os.environ.get('ART_FORCE_TRY_PREDICATED_SIMD') == 'true'
  //
  //      HalvingAdd idiom is not supported for SVE.
  ///     CHECK-NOT: VecHalvingAdd
  //
  /// CHECK-ELSE:
  //
  ///     CHECK-DAG: <<SMAX:i\d+>> IntConstant 32767                     loop:none
  ///     CHECK-DAG: <<Repl:d\d+>> VecReplicateScalar [<<SMAX>>]         loop:none
  ///     CHECK-DAG: <<Get:d\d+>>  VecLoad                               loop:<<Loop:B\d+>> outer_loop:none
  ///     CHECK-DAG: <<HAdd:d\d+>> VecHalvingAdd [<<Get>>,<<Repl>>] packed_type:Int16 rounded:false loop:<<Loop>> outer_loop:none
  ///     CHECK-DAG:               VecStore [{{l\d+}},{{i\d+}},<<HAdd>>] loop:<<Loop>>      outer_loop:none
  //
  /// CHECK-FI:
  private static void halving_add_signed_constant(short[] b1, short[] bo) {
    int min_length = Math.min(bo.length, b1.length);
    for (int i = 0; i < min_length; i++) {
      bo[i] = (short) ((b1[i] + 0x7fff) >>> 1);
    }
  }

  /// CHECK-START: void HaddAltShort.halving_add_unsigned_constant(short[], short[]) instruction_simplifier (before)
  /// CHECK-DAG: <<I1:i\d+>>   IntConstant 1                       loop:none
  /// CHECK-DAG: <<UMAX:i\d+>> IntConstant 65535                   loop:none
  /// CHECK-DAG: <<Phi:i\d+>>  Phi                                 loop:<<Loop:B\d+>> outer_loop:none
  /// CHECK-DAG: <<Get:s\d+>>  ArrayGet                            loop:<<Loop>>      outer_loop:none
  /// CHECK-DAG: <<And:i\d+>>  And [<<Get>>,<<UMAX>>]              loop:<<Loop>>      outer_loop:none
  /// CHECK-DAG: <<Add:i\d+>>  Add [<<And>>,<<UMAX>>]              loop:<<Loop>>      outer_loop:none
  /// CHECK-DAG: <<UShr:i\d+>> UShr [<<Add>>,<<I1>>]               loop:<<Loop>>      outer_loop:none
  /// CHECK-DAG: <<Cnv:s\d+>>  TypeConversion [<<UShr>>]           loop:<<Loop>>      outer_loop:none
  /// CHECK-DAG:               ArraySet [{{l\d+}},{{i\d+}},<<Cnv>>] loop:<<Loop>>      outer_loop:none
  //
  /// CHECK-START: void HaddAltShort.halving_add_unsigned_constant(short[], short[]) loop_optimization (before)
  /// CHECK-DAG: <<I1:i\d+>>   IntConstant 1                       loop:none
  /// CHECK-DAG: <<UMAX:i\d+>> IntConstant 65535                   loop:none
  /// CHECK-DAG: <<Phi:i\d+>>  Phi                                 loop:<<Loop:B\d+>> outer_loop:none
  /// CHECK-DAG: <<Get:c\d+>>  ArrayGet                            loop:<<Loop>>      outer_loop:none
  /// CHECK-DAG: <<Add:i\d+>>  Add [<<Get>>,<<UMAX>>]              loop:<<Loop>>      outer_loop:none
  /// CHECK-DAG: <<UShr:i\d+>> UShr [<<Add>>,<<I1>>]               loop:<<Loop>>      outer_loop:none
  /// CHECK-DAG: <<Cnv:s\d+>>  TypeConversion [<<UShr>>]           loop:<<Loop>>      outer_loop:none
  /// CHECK-DAG:               ArraySet [{{l\d+}},<<Phi>>,<<Cnv>>] loop:<<Loop>>      outer_loop:none
  //
  /// CHECK-START-ARM: void HaddAltShort.halving_add_unsigned_constant(short[], short[]) loop_optimization (after)
  /// CHECK-DAG: <<UMAX:i\d+>> IntConstant 65535                     loop:none
  /// CHECK-DAG: <<Repl:d\d+>> VecReplicateScalar [<<UMAX>>]         loop:none
  /// CHECK-DAG: <<Get:d\d+>>  VecLoad                               loop:<<Loop:B\d+>> outer_loop:none
  /// CHECK-DAG: <<HAdd:d\d+>> VecHalvingAdd [<<Get>>,<<Repl>>] packed_type:Uint16 rounded:false loop:<<Loop>> outer_loop:none
  /// CHECK-DAG:               VecStore [{{l\d+}},{{i\d+}},<<HAdd>>] loop:<<Loop>>      outer_loop:none
  //
  /// CHECK-START-ARM64: void HaddAltShort.halving_add_unsigned_constant(short[], short[]) loop_optimization (after)
  /// CHECK-IF:     hasIsaFeature("sve") and os.environ.get('ART_FORCE_TRY_PREDICATED_SIMD') == 'true'
  //
  //      HalvingAdd idiom is not supported for SVE.
  ///     CHECK-NOT: VecHalvingAdd
  //
  /// CHECK-ELSE:
  //
  ///     CHECK-DAG: <<UMAX:i\d+>> IntConstant 65535                     loop:none
  ///     CHECK-DAG: <<Repl:d\d+>> VecReplicateScalar [<<UMAX>>]         loop:none
  ///     CHECK-DAG: <<Get:d\d+>>  VecLoad                               loop:<<Loop:B\d+>> outer_loop:none
  ///     CHECK-DAG: <<HAdd:d\d+>> VecHalvingAdd [<<Get>>,<<Repl>>] packed_type:Uint16 rounded:false loop:<<Loop>> outer_loop:none
  ///     CHECK-DAG:               VecStore [{{l\d+}},{{i\d+}},<<HAdd>>] loop:<<Loop>>      outer_loop:none
  //
  /// CHECK-FI:
  private static void halving_add_unsigned_constant(short[] b1, short[] bo) {
    int min_length = Math.min(bo.length, b1.length);
    for (int i = 0; i < min_length; i++) {
      bo[i] = (short) (((b1[i] & 0xffff) + 0xffff) >>> 1);
    }
  }

  public static void main() {
    // Some interesting values.
    short[] interesting = {
      (short) 0x0000,
      (short) 0x0001,
      (short) 0x0002,
      (short) 0x1234,
      (short) 0x8000,
      (short) 0x8001,
      (short) 0x7fff,
      (short) 0xffff
    };
    // Initialize cross-values to test all cases, and also
    // set up some extra values to exercise the cleanup loop.
    for (int i = 0; i < M; i++) {
      sB1[i] = (short) i;
      sB2[i] = interesting[i & 7];
    }

    // Test halving add idioms. Note that the expected result is computed
    // with the arithmetic >> to demonstrate the computed narrower result
    // does not depend on the wider >> or >>>.
    halving_add_signed(sB1, sB2, sBo);
    for (int i = 0; i < M; i++) {
      short e = (short) ((sB1[i] + sB2[i]) >> 1);
      expectEquals(e, sBo[i]);
    }
    halving_add_unsigned(sB1, sB2, sBo);
    for (int i = 0; i < M; i++) {
      short e = (short) (((sB1[i] & 0xffff) + (sB2[i] & 0xffff)) >> 1);
      expectEquals(e, sBo[i]);
    }
    rounding_halving_add_signed(sB1, sB2, sBo);
    for (int i = 0; i < M; i++) {
      short e = (short) ((sB1[i] + sB2[i] + 1) >> 1);
      expectEquals(e, sBo[i]);
    }
    rounding_halving_add_unsigned(sB1, sB2, sBo);
    for (int i = 0; i < M; i++) {
      short e = (short) (((sB1[i] & 0xffff) + (sB2[i] & 0xffff) + 1) >> 1);
      expectEquals(e, sBo[i]);
    }
    halving_add_signed_constant(sB1, sBo);
    for (int i = 0; i < M; i++) {
      short e = (short) ((sB1[i] + 0x7fff) >> 1);
      expectEquals(e, sBo[i]);
    }
    halving_add_unsigned_constant(sB1, sBo);
    for (int i = 0; i < M; i++) {
      short e = (short) (((sB1[i] & 0xffff) + 0xffff) >> 1);
      expectEquals(e, sBo[i]);
    }

    System.out.println("HaddAltShort passed");
  }

  private static void expectEquals(int expected, int result) {
    if (expected != result) {
      throw new Error("Expected: " + expected + ", found: " + result);
    }
  }
}
