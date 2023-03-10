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

public class Main {

  public static void assertIntEquals(int expected, int result) {
    if (expected != result) {
      throw new Error("Expected: " + expected + ", found: " + result);
    }
  }

  /// CHECK-START-ARM64: void Main.checkIntCase(int[]) instruction_simplifier_arm64 (before)
  /// CHECK-DAG:             <<Array:l\d+>>         ParameterValue
  /// CHECK-DAG:             <<Const5:i\d+>>        IntConstant 5
  /// CHECK-IF:     hasIsaFeature("sve") and os.environ.get('ART_FORCE_TRY_PREDICATED_SIMD') == 'true'
  //
  ///     CHECK-DAG:             <<Repl:d\d+>>          VecReplicateScalar [<<Const5>>,{{j\d+}}]
  //      -------------- Loop
  ///     CHECK-DAG:             <<Index:i\d+>>         Phi
  ///     CHECK-DAG:             <<LoopP:j\d+>>         VecPredWhile
  ///     CHECK-DAG:                                    If
  ///     CHECK-DAG:             <<Load:d\d+>>          VecLoad [<<Array>>,<<Index>>,<<LoopP>>]
  ///     CHECK-DAG:             <<Add:d\d+>>           VecAdd [<<Load>>,<<Repl>>,<<LoopP>>]
  ///     CHECK-DAG:                                    VecStore [<<Array>>,<<Index>>,<<Add>>,<<LoopP>>]
  //
  /// CHECK-ELSE:
  //
  ///     CHECK-DAG:             <<Repl:d\d+>>          VecReplicateScalar [<<Const5>>]
  //      -------------- Loop
  ///     CHECK-DAG:             <<Index:i\d+>>         Phi
  ///     CHECK-DAG:                                    If
  ///     CHECK-DAG:             <<Load:d\d+>>          VecLoad [<<Array>>,<<Index>>]
  ///     CHECK-DAG:             <<Add:d\d+>>           VecAdd [<<Load>>,<<Repl>>]
  ///     CHECK-DAG:                                    VecStore [<<Array>>,<<Index>>,<<Add>>]
  //
  /// CHECK-FI:

  /// CHECK-START-ARM64: void Main.checkIntCase(int[]) instruction_simplifier_arm64 (after)
  /// CHECK-DAG:             <<Array:l\d+>>         ParameterValue
  /// CHECK-DAG:             <<Const5:i\d+>>        IntConstant 5
  /// CHECK-IF:     hasIsaFeature("sve") and os.environ.get('ART_FORCE_TRY_PREDICATED_SIMD') == 'true'
  //
  //      IntermediateAddressIndex is not supported for SVE.
  ///     CHECK-DAG:             <<Repl:d\d+>>          VecReplicateScalar [<<Const5>>,{{j\d+}}]
  //      -------------- Loop
  ///     CHECK-DAG:             <<LoopP:j\d+>>         VecPredWhile
  ///     CHECK-DAG:             <<Index:i\d+>>         Phi
  ///     CHECK-DAG:                                    If
  ///     CHECK-DAG:             <<IntAddr1:i\d+>>      IntermediateAddress [<<Array>>,{{i\d+}}]
  ///     CHECK-DAG:             <<Load:d\d+>>          VecLoad [<<IntAddr1>>,<<Index>>,<<LoopP>>]
  ///     CHECK-DAG:             <<Add:d\d+>>           VecAdd [<<Load>>,<<Repl>>,<<LoopP>>]
  ///     CHECK-DAG:             <<IntAddr2:i\d+>>      IntermediateAddress [<<Array>>,{{i\d+}}]
  ///     CHECK-DAG:                                    VecStore [<<IntAddr2>>,<<Index>>,<<Add>>,<<LoopP>>]
  //
  /// CHECK-ELSE:
  //
  ///     CHECK-DAG:             <<Const2:i\d+>>        IntConstant 2
  ///     CHECK-DAG:             <<DataOffset:i\d+>>    IntConstant 12
  ///     CHECK-DAG:             <<Repl:d\d+>>          VecReplicateScalar [<<Const5>>]
  //      -------------- Loop
  ///     CHECK-DAG:             <<Index:i\d+>>         Phi
  ///     CHECK-DAG:                                    If
  ///     CHECK-DAG:             <<Address1:i\d+>>      IntermediateAddressIndex [<<Index>>,<<DataOffset>>,<<Const2>>]
  ///     CHECK-DAG:             <<Load:d\d+>>          VecLoad [<<Array>>,<<Address1>>]
  ///     CHECK-DAG:             <<Add:d\d+>>           VecAdd [<<Load>>,<<Repl>>]
  ///     CHECK-DAG:             <<Address2:i\d+>>      IntermediateAddressIndex [<<Index>>,<<DataOffset>>,<<Const2>>]
  ///     CHECK-DAG:                                    VecStore [<<Array>>,<<Address2>>,<<Add>>]
  //
  /// CHECK-FI:

  /// CHECK-START-ARM64: void Main.checkIntCase(int[]) GVN$after_arch (after)
  /// CHECK-DAG:             <<Array:l\d+>>         ParameterValue
  /// CHECK-DAG:             <<Const5:i\d+>>        IntConstant 5
  /// CHECK-IF:     hasIsaFeature("sve") and os.environ.get('ART_FORCE_TRY_PREDICATED_SIMD') == 'true'
  //
  //      IntermediateAddressIndex is not supported for SVE.
  ///     CHECK-DAG:             <<Repl:d\d+>>          VecReplicateScalar [<<Const5>>,{{j\d+}}]
  //      -------------- Loop
  ///     CHECK-DAG:             <<LoopP:j\d+>>         VecPredWhile
  ///     CHECK-DAG:             <<Index:i\d+>>         Phi
  ///     CHECK-DAG:                                    If
  ///     CHECK-DAG:             <<IntAddr:i\d+>>       IntermediateAddress [<<Array>>,{{i\d+}}]
  ///     CHECK-DAG:             <<Load:d\d+>>          VecLoad [<<IntAddr>>,<<Index>>,<<LoopP>>]
  ///     CHECK-DAG:             <<Add:d\d+>>           VecAdd [<<Load>>,<<Repl>>,<<LoopP>>]
  ///     CHECK-DAG:                                    VecStore [<<IntAddr>>,<<Index>>,<<Add>>,<<LoopP>>]
  //
  /// CHECK-ELSE:
  //
  ///     CHECK-DAG:             <<DataOffset:i\d+>>    IntConstant 12
  ///     CHECK-DAG:             <<Const2:i\d+>>        IntConstant 2
  ///     CHECK-DAG:             <<Repl:d\d+>>          VecReplicateScalar [<<Const5>>]
  //      -------------- Loop
  ///     CHECK-DAG:             <<Index:i\d+>>         Phi
  ///     CHECK-DAG:                                    If
  ///     CHECK-DAG:             <<Address1:i\d+>>      IntermediateAddressIndex [<<Index>>,<<DataOffset>>,<<Const2>>]
  ///     CHECK-DAG:             <<Load:d\d+>>          VecLoad [<<Array>>,<<Address1>>]
  ///     CHECK-DAG:             <<Add:d\d+>>           VecAdd [<<Load>>,<<Repl>>]
  ///     CHECK-NOT:                                    IntermediateAddress
  ///     CHECK-DAG:                                    VecStore [<<Array>>,<<Address1>>,<<Add>>]
  //
  /// CHECK-FI:

  /// CHECK-START-ARM64: void Main.checkIntCase(int[]) disassembly (after)
  /// CHECK-IF:     hasIsaFeature("sve") and os.environ.get('ART_FORCE_TRY_PREDICATED_SIMD') == 'true'
  //
  //      IntermediateAddressIndex is not supported for SVE.
  ///     CHECK-NOT:                                    IntermediateAddressIndex
  //
  /// CHECK-ELSE:
  //
  ///     CHECK:                                        IntermediateAddressIndex
  ///     CHECK-NEXT:                                   add w{{[0-9]+}}, w{{[0-9]+}}, w{{[0-9]+}}, lsl #2
  //
  /// CHECK-FI:
  public static void checkIntCase(int[] a) {
    for (int i = 0; i < 128; i++) {
      a[i] += 5;
    }
  }

  /// CHECK-START-ARM64: void Main.checkByteCase(byte[]) instruction_simplifier_arm64 (before)
  /// CHECK-DAG:             <<Array:l\d+>>         ParameterValue
  /// CHECK-DAG:             <<Const5:i\d+>>        IntConstant 5
  /// CHECK-IF:     hasIsaFeature("sve") and os.environ.get('ART_FORCE_TRY_PREDICATED_SIMD') == 'true'
  //
  ///     CHECK-DAG:             <<Repl:d\d+>>          VecReplicateScalar [<<Const5>>,{{j\d+}}]
  //      -------------- Loop
  ///     CHECK-DAG:             <<LoopP:j\d+>>         VecPredWhile
  ///     CHECK-DAG:             <<Index:i\d+>>         Phi
  ///     CHECK-DAG:                                    If
  ///     CHECK-DAG:             <<Load:d\d+>>          VecLoad [<<Array>>,<<Index>>,<<LoopP>>]
  ///     CHECK-DAG:             <<Add:d\d+>>           VecAdd [<<Load>>,<<Repl>>,<<LoopP>>]
  ///     CHECK-DAG:                                    VecStore [<<Array>>,<<Index>>,<<Add>>,<<LoopP>>]
  //
  /// CHECK-ELSE:
  //
  ///     CHECK-DAG:             <<Repl:d\d+>>          VecReplicateScalar [<<Const5>>]
  //      -------------- Loop
  ///     CHECK-DAG:             <<Index:i\d+>>         Phi
  ///     CHECK-DAG:                                    If
  ///     CHECK-DAG:             <<Load:d\d+>>          VecLoad [<<Array>>,<<Index>>]
  ///     CHECK-DAG:             <<Add:d\d+>>           VecAdd [<<Load>>,<<Repl>>]

  //
  /// CHECK-FI:

  /// CHECK-START-ARM64: void Main.checkByteCase(byte[]) instruction_simplifier_arm64 (after)
  /// CHECK-DAG:             <<Array:l\d+>>         ParameterValue
  /// CHECK-DAG:             <<Const0:i\d+>>        IntConstant 0
  /// CHECK-DAG:             <<Const5:i\d+>>        IntConstant 5
  /// CHECK-IF:     hasIsaFeature("sve") and os.environ.get('ART_FORCE_TRY_PREDICATED_SIMD') == 'true'
  //
  //      IntermediateAddressIndex is not supported for SVE.
  ///     CHECK-DAG:             <<Repl:d\d+>>          VecReplicateScalar [<<Const5>>,{{j\d+}}]
  //      -------------- Loop
  ///     CHECK-DAG:             <<LoopP:j\d+>>         VecPredWhile
  ///     CHECK-DAG:             <<Index:i\d+>>         Phi
  ///     CHECK-DAG:                                    If
  ///     CHECK-DAG:             <<IntAddr1:i\d+>>      IntermediateAddress [<<Array>>,{{i\d+}}]
  ///     CHECK-DAG:             <<Load:d\d+>>          VecLoad [<<IntAddr1>>,<<Index>>,<<LoopP>>]
  ///     CHECK-DAG:             <<Add:d\d+>>           VecAdd [<<Load>>,<<Repl>>,<<LoopP>>]
  ///     CHECK-DAG:             <<IntAddr2:i\d+>>      IntermediateAddress [<<Array>>,{{i\d+}}]
  ///     CHECK-DAG:                                    VecStore [<<IntAddr2>>,<<Index>>,<<Add>>,<<LoopP>>]
  //
  /// CHECK-ELSE:
  //
  ///     CHECK-DAG:             <<DataOffset:i\d+>>    IntConstant 12
  ///     CHECK-DAG:             <<Repl:d\d+>>          VecReplicateScalar [<<Const5>>]
  //      -------------- Loop
  ///     CHECK-DAG:             <<Index:i\d+>>         Phi
  ///     CHECK-DAG:                                    If
  ///     CHECK-DAG:             <<Address1:i\d+>>      IntermediateAddressIndex [<<Index>>,<<DataOffset>>,<<Const0>>]
  ///     CHECK-DAG:             <<Load:d\d+>>          VecLoad [<<Array>>,<<Address1>>]
  ///     CHECK-DAG:             <<Add:d\d+>>           VecAdd [<<Load>>,<<Repl>>]
  ///     CHECK-DAG:             <<Address2:i\d+>>      IntermediateAddressIndex [<<Index>>,<<DataOffset>>,<<Const0>>]
  ///     CHECK-DAG:                                    VecStore [<<Array>>,<<Address2>>,<<Add>>]
  //
  /// CHECK-FI:

  /// CHECK-START-ARM64: void Main.checkByteCase(byte[]) GVN$after_arch (after)
  /// CHECK-DAG:             <<Array:l\d+>>         ParameterValue
  /// CHECK-DAG:             <<Const0:i\d+>>        IntConstant 0
  /// CHECK-DAG:             <<Const5:i\d+>>        IntConstant 5
  /// CHECK-IF:     hasIsaFeature("sve") and os.environ.get('ART_FORCE_TRY_PREDICATED_SIMD') == 'true'
  //
  //      IntermediateAddressIndex is not supported for SVE.
  ///     CHECK-DAG:             <<Repl:d\d+>>          VecReplicateScalar [<<Const5>>,{{j\d+}}]
  //      -------------- Loop
  ///     CHECK-DAG:             <<LoopP:j\d+>>         VecPredWhile
  ///     CHECK-DAG:             <<Index:i\d+>>         Phi
  ///     CHECK-DAG:                                    If
  ///     CHECK-DAG:             <<IntAddr:i\d+>>       IntermediateAddress [<<Array>>,{{i\d+}}]
  ///     CHECK-DAG:             <<Load:d\d+>>          VecLoad [<<IntAddr>>,<<Index>>,<<LoopP>>]
  ///     CHECK-DAG:             <<Add:d\d+>>           VecAdd [<<Load>>,<<Repl>>,<<LoopP>>]
  ///     CHECK-DAG:                                    VecStore [<<IntAddr>>,<<Index>>,<<Add>>,<<LoopP>>]
  //
  /// CHECK-ELSE:
  //
  ///     CHECK-DAG:             <<DataOffset:i\d+>>    IntConstant 12
  ///     CHECK-DAG:             <<Repl:d\d+>>          VecReplicateScalar [<<Const5>>]
  //      -------------- Loop
  ///     CHECK-DAG:             <<Index:i\d+>>         Phi
  ///     CHECK-DAG:                                    If
  ///     CHECK-DAG:             <<Address1:i\d+>>      IntermediateAddressIndex [<<Index>>,<<DataOffset>>,<<Const0>>]
  ///     CHECK-DAG:             <<Load:d\d+>>          VecLoad [<<Array>>,<<Address1>>]
  ///     CHECK-DAG:             <<Add:d\d+>>           VecAdd [<<Load>>,<<Repl>>]
  ///     CHECK-NOT:                                    IntermediateAddress
  ///     CHECK-DAG:                                    VecStore [<<Array>>,<<Address1>>,<<Add>>]
  //
  /// CHECK-FI:

  /// CHECK-START-ARM64: void Main.checkByteCase(byte[]) disassembly (after)
  /// CHECK-IF:     hasIsaFeature("sve") and os.environ.get('ART_FORCE_TRY_PREDICATED_SIMD') == 'true'
  //
  //      IntermediateAddressIndex is not supported for SVE.
  ///     CHECK-NOT:                                    IntermediateAddressIndex
  //
  /// CHECK-ELSE:
  //
  ///     CHECK:                                        IntermediateAddressIndex
  ///     CHECK-NEXT:                                   add w{{[0-9]+}}, w{{[0-9]+}}, #0x{{[0-9a-fA-F]+}}
  ///     CHECK:                                        VecLoad
  ///     CHECK-NEXT:                                   ldr q{{[0-9]+}}, [x{{[0-9]+}}, x{{[0-9]+}}]
  ///     CHECK:                                        VecStore
  ///     CHECK-NEXT:                                   str q{{[0-9]+}}, [x{{[0-9]+}}, x{{[0-9]+}}]
  //
  /// CHECK-FI:
  public static void checkByteCase(byte[] a) {
    for (int i = 0; i < 128; i++) {
      a[i] += 5;
    }
  }

  /// CHECK-START-ARM64: void Main.checkSingleAccess(int[]) instruction_simplifier_arm64 (before)
  /// CHECK-DAG:             <<Array:l\d+>>         ParameterValue
  /// CHECK-DAG:             <<Const5:i\d+>>        IntConstant 5
  /// CHECK-IF:     hasIsaFeature("sve") and os.environ.get('ART_FORCE_TRY_PREDICATED_SIMD') == 'true'
  //
  ///     CHECK-DAG:             <<Repl:d\d+>>          VecReplicateScalar [<<Const5>>,{{j\d+}}]
  //      -------------- Loop
  ///     CHECK-DAG:             <<Index:i\d+>>         Phi
  ///     CHECK-DAG:             <<LoopP:j\d+>>         VecPredWhile
  ///     CHECK-DAG:                                    If
  ///     CHECK-DAG:                                    VecStore [<<Array>>,<<Index>>,<<Repl>>,<<LoopP>>]
  //
  /// CHECK-ELSE:
  //
  ///     CHECK-DAG:             <<Repl:d\d+>>          VecReplicateScalar [<<Const5>>]
  //      -------------- Loop
  ///     CHECK-DAG:             <<Index:i\d+>>         Phi
  ///     CHECK-DAG:                                    If
  ///     CHECK-DAG:                                    VecStore [<<Array>>,<<Index>>,<<Repl>>]
  //
  /// CHECK-FI:

  /// CHECK-START-ARM64: void Main.checkSingleAccess(int[]) instruction_simplifier_arm64 (after)
  /// CHECK-DAG:             <<Array:l\d+>>         ParameterValue
  /// CHECK-DAG:             <<Const0:i\d+>>        IntConstant 0
  /// CHECK-DAG:             <<Const5:i\d+>>        IntConstant 5
  /// CHECK-IF:     hasIsaFeature("sve") and os.environ.get('ART_FORCE_TRY_PREDICATED_SIMD') == 'true'
  //
  ///     CHECK-DAG:             <<Repl:d\d+>>          VecReplicateScalar [<<Const5>>,{{j\d+}}]
  //      -------------- Loop
  ///     CHECK-DAG:             <<LoopP:j\d+>>         VecPredWhile
  ///     CHECK-DAG:             <<Index:i\d+>>         Phi
  ///     CHECK-DAG:                                    If
  ///     CHECK-DAG:             <<IntAddr:i\d+>>       IntermediateAddress [<<Array>>,{{i\d+}}]
  ///     CHECK-DAG:                                    VecStore [<<IntAddr>>,<<Index>>,<<Repl>>,<<LoopP>>]
  //
  /// CHECK-ELSE:
  //
  ///     CHECK-DAG:             <<Repl:d\d+>>          VecReplicateScalar [<<Const5>>]
  //      -------------- Loop
  ///     CHECK-DAG:             <<Index:i\d+>>         Phi
  ///     CHECK-DAG:                                    If
  ///     CHECK-DAG:                                    VecStore [<<Array>>,<<Index>>,<<Repl>>]
  //
  /// CHECK-FI:
  //
  /// CHECK-NOT:                                    IntermediateAddress
  public static void checkSingleAccess(int[] a) {
    for (int i = 0; i < 128; i++) {
      a[i] = 5;
    }
  }

  /// CHECK-START-ARM64: void Main.checkInt2Float(int[], float[]) instruction_simplifier_arm64 (before)
  /// CHECK-DAG:             <<Array1:l\d+>>        ParameterValue
  /// CHECK-DAG:             <<Array2:l\d+>>        ParameterValue
  //  -------------- Loop
  /// CHECK-IF:     hasIsaFeature("sve") and os.environ.get('ART_FORCE_TRY_PREDICATED_SIMD') == 'true'
  //
  ///     CHECK-DAG:             <<LoopP:j\d+>>         VecPredWhile
  ///     CHECK-DAG:             <<Index:i\d+>>         Phi
  ///     CHECK-DAG:                                    If
  ///     CHECK-DAG:             <<Load:d\d+>>          VecLoad [<<Array1>>,<<Index>>,<<LoopP>>]
  ///     CHECK-DAG:             <<Cnv:d\d+>>           VecCnv [<<Load>>,<<LoopP>>]
  ///     CHECK-DAG:                                    VecStore [<<Array2>>,<<Index>>,<<Cnv>>,<<LoopP>>]
  //
  /// CHECK-ELSE:
  //
  ///     CHECK-DAG:             <<Index:i\d+>>         Phi
  ///     CHECK-DAG:                                    If
  ///     CHECK-DAG:             <<Load:d\d+>>          VecLoad [<<Array1>>,<<Index>>]
  ///     CHECK-DAG:             <<Cnv:d\d+>>           VecCnv [<<Load>>]
  ///     CHECK-DAG:                                    VecStore [<<Array2>>,<<Index>>,<<Cnv>>]
  //
  /// CHECK-FI:

  /// CHECK-START-ARM64: void Main.checkInt2Float(int[], float[]) instruction_simplifier_arm64 (after)
  /// CHECK-DAG:             <<Array1:l\d+>>        ParameterValue
  /// CHECK-DAG:             <<Array2:l\d+>>        ParameterValue
  /// CHECK-IF:     hasIsaFeature("sve") and os.environ.get('ART_FORCE_TRY_PREDICATED_SIMD') == 'true'
  //
  //      IntermediateAddressIndex is not supported for SVE.
  //      -------------- Loop
  ///     CHECK-DAG:             <<LoopP:j\d+>>         VecPredWhile
  ///     CHECK-DAG:             <<Index:i\d+>>         Phi
  ///     CHECK-DAG:                                    If
  ///     CHECK-DAG:             <<IntAddr1:i\d+>>      IntermediateAddress [<<Array1>>,{{i\d+}}]
  ///     CHECK-DAG:             <<Load:d\d+>>          VecLoad [<<IntAddr1>>,<<Index>>,<<LoopP>>]
  ///     CHECK-DAG:             <<Cnv:d\d+>>           VecCnv [<<Load>>,<<LoopP>>]
  ///     CHECK-DAG:             <<IntAddr2:i\d+>>      IntermediateAddress [<<Array2>>,{{i\d+}}]
  ///     CHECK-DAG:                                    VecStore [<<IntAddr2>>,<<Index>>,<<Cnv>>,<<LoopP>>]
  //
  /// CHECK-ELSE:
  //
  ///     CHECK-DAG:             <<DataOffset:i\d+>>    IntConstant 12
  ///     CHECK-DAG:             <<Const2:i\d+>>        IntConstant 2
  //      -------------- Loop
  ///     CHECK-DAG:             <<Index:i\d+>>         Phi
  ///     CHECK-DAG:                                    If
  ///     CHECK-DAG:             <<Address1:i\d+>>      IntermediateAddressIndex [<<Index>>,<<DataOffset>>,<<Const2>>]
  ///     CHECK-DAG:             <<Load:d\d+>>          VecLoad [<<Array1>>,<<Address1>>]
  ///     CHECK-DAG:             <<Cnv:d\d+>>           VecCnv [<<Load>>]
  ///     CHECK-DAG:             <<Address2:i\d+>>      IntermediateAddressIndex [<<Index>>,<<DataOffset>>,<<Const2>>]
  ///     CHECK-DAG:                                    VecStore [<<Array2>>,<<Address2>>,<<Cnv>>]
  //
  /// CHECK-FI:

  /// CHECK-START-ARM64: void Main.checkInt2Float(int[], float[]) GVN$after_arch (after)
  /// CHECK-DAG:             <<Array1:l\d+>>        ParameterValue
  /// CHECK-DAG:             <<Array2:l\d+>>        ParameterValue
  /// CHECK-IF:     hasIsaFeature("sve") and os.environ.get('ART_FORCE_TRY_PREDICATED_SIMD') == 'true'
  //
  //      IntermediateAddressIndex is not supported for SVE.
  //      -------------- Loop
  ///     CHECK-DAG:             <<LoopP:j\d+>>         VecPredWhile
  ///     CHECK-DAG:             <<Index:i\d+>>         Phi
  ///     CHECK-DAG:                                    If
  ///     CHECK-DAG:             <<IntAddr1:i\d+>>      IntermediateAddress [<<Array1>>,{{i\d+}}]
  ///     CHECK-DAG:             <<Load:d\d+>>          VecLoad [<<IntAddr1>>,<<Index>>,<<LoopP>>]
  ///     CHECK-DAG:             <<Cnv:d\d+>>           VecCnv [<<Load>>,<<LoopP>>]
  ///     CHECK-DAG:             <<IntAddr2:i\d+>>      IntermediateAddress [<<Array2>>,{{i\d+}}]
  ///     CHECK-DAG:                                    VecStore [<<IntAddr2>>,<<Index>>,<<Cnv>>,<<LoopP>>]
  //
  /// CHECK-ELSE:
  //
  ///     CHECK-DAG:             <<DataOffset:i\d+>>    IntConstant 12
  ///     CHECK-DAG:             <<Const2:i\d+>>        IntConstant 2
  //      -------------- Loop
  ///     CHECK-DAG:             <<Index:i\d+>>         Phi
  ///     CHECK-DAG:                                    If
  ///     CHECK-DAG:             <<Address1:i\d+>>      IntermediateAddressIndex [<<Index>>,<<DataOffset>>,<<Const2>>]
  ///     CHECK-DAG:             <<Load:d\d+>>          VecLoad [<<Array1>>,<<Address1>>]
  ///     CHECK-DAG:             <<Cnv:d\d+>>           VecCnv [<<Load>>]
  ///     CHECK-NOT:                                    IntermediateAddress
  ///     CHECK-DAG:                                    VecStore [<<Array2>>,<<Address1>>,<<Cnv>>]
  //
  /// CHECK-FI:

  /// CHECK-START-ARM64: void Main.checkInt2Float(int[], float[]) disassembly (after)
  /// CHECK-IF:     hasIsaFeature("sve") and os.environ.get('ART_FORCE_TRY_PREDICATED_SIMD') == 'true'
  //
  //      IntermediateAddressIndex is not supported for SVE.
  ///     CHECK-NOT:                                    IntermediateAddressIndex
  //
  /// CHECK-ELSE:
  //
  ///     CHECK:                                        IntermediateAddressIndex
  ///     CHECK-NEXT:                                   add w{{[0-9]+}}, w{{[0-9]+}}, w{{[0-9]+}}, lsl #2
  //
  /// CHECK-FI:
  public static void checkInt2Float(int[] a, float[] b) {
    for (int i = 0; i < 128; i++) {
      b[i] = (float) a[i];
    }
  }

  public static final int ARRAY_SIZE = 1024;

  public static int calcArraySum(int[] a, byte[] b, float[] c) {
    int sum = 0;
    for (int i = 0; i < 128; i++) {
      sum += a[i] + b[i] + (int) c[i];
    }
    return sum;
  }

  public static void main(String[] args) {
    byte[] ba = new byte[ARRAY_SIZE];
    int[] ia = new int[ARRAY_SIZE];
    float[] fa = new float[ARRAY_SIZE];

    checkSingleAccess(ia);
    checkIntCase(ia);
    checkByteCase(ba);
    checkInt2Float(ia, fa);

    assertIntEquals(3200, calcArraySum(ia, ba, fa));
  }
}
