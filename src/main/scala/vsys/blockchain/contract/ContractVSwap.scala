package vsys.blockchain.contract

import com.google.common.primitives.{Ints, Longs}
import vsys.blockchain.contract.ContractGen._
import vsys.blockchain.state._
import vsys.utils.serialization.Deser

object ContractVSwap {
  lazy val contract: Contract = Contract.buildContract(Deser.serilizeString("vdds"), Ints.toByteArray(2),
    Seq(), // Triggers
    Seq(), // Functions
    stateVarSeq, // StateVars
    stateMapSeq, // StateMaps
    Seq()  // Textual
  ).explicitGet()

  // State Var
  val stateVarName = List("maker", "tokenAId", "tokenBId", "tokenLiquidityId", "swapStatus", "minimumLiquidity",
                          "tokenAReserved", "tokenBReserved", "totalSupply", "liquidityTokenLeft")
  val makerStateVar: StateVar               = StateVar(0.toByte, DataType.Address.id.toByte)
  val tokenAIdStateVar: StateVar            = StateVar(1.toByte, DataType.TokenId.id.toByte)
  val tokeBIdStateVar: StateVar             = StateVar(2.toByte, DataType.TokenId.id.toByte)
  val tokenLiquidityIdStateVar: StateVar    = StateVar(3.toByte, DataType.TokenId.id.toByte)
  val swapStatusStateVar: StateVar          = StateVar(4.toByte, DataType.Boolean.id.toByte)
  val minimumLiquidityStateVar: StateVar    = StateVar(5.toByte, DataType.Amount.id.toByte)
  val tokenAReservedStateVar: StateVar      = StateVar(6.toByte, DataType.Amount.id.toByte)
  val tokenBReservedStateVar: StateVar      = StateVar(7.toByte, DataType.Amount.id.toByte)
  val totalSupplyStateVar: StateVar         = StateVar(8.toByte, DataType.Amount.id.toByte)
  val liquidityTokenLeftStateVar: StateVar  = StateVar(9.toByte, DataType.Amount.id.toByte)
  lazy val stateVarSeq = Seq(makerStateVar.arr, tokenAIdStateVar.arr, tokeBIdStateVar.arr, tokenLiquidityIdStateVar.arr,
                             swapStatusStateVar.arr, minimumLiquidityStateVar.arr, tokenAReservedStateVar.arr,
                             tokenBReservedStateVar.arr, totalSupplyStateVar.arr, liquidityTokenLeftStateVar.arr)
  lazy val stateVarTextual: Array[Byte] = Deser.serializeArrays(stateVarName.map(x => Deser.serilizeString(x)))

  // State Map
  val stateMapTokenABalance         = List("tokenABalance", "userAddress", "balance")
  val stateMapTokenBBalance         = List("tokenBBalance", "userAddress", "balance")
  val stateMapLiquidityBalance      = List("liquidityBalance", "userAddress", "balance")
  val tokenABalanceMap: StateMap    = StateMap(0.toByte, DataType.Address.id.toByte, DataType.Amount.id.toByte)
  val tokenBBalanceMap: StateMap    = StateMap(1.toByte, DataType.Address.id.toByte, DataType.Amount.id.toByte)
  val liquidityBalanceMap: StateMap = StateMap(2.toByte, DataType.Address.id.toByte, DataType.Amount.id.toByte)
  lazy val stateMapSeq = Seq(tokenABalanceMap.arr, tokenBBalanceMap.arr, liquidityBalanceMap.arr)
  lazy val stateMapTextual: Array[Byte] = textualStateMap(Seq(stateMapTokenABalance, stateMapTokenBBalance, stateMapLiquidityBalance))

  // Initialization Trigger

  // Deposit Trigger

  // WithDraw Trigger

  // Functions
  // Supersede
  val supersedeId: Short = 0
  val supersedePara: Seq[String] = Seq("newOwner",
                                       "maker")
  val supersedeDataType: Array[Byte] = Array(DataType.Account.id.toByte)
  val supersedeOpcs: Seq[Array[Byte]] =  Seq(
    cdbvrGet ++ Array(makerStateVar.index, 1.toByte),
    assertSigner ++ Array(1.toByte),
    cdbvSet ++ Array(makerStateVar.index, 0.toByte))
  lazy val supersedeFunc: Array[Byte] = getFunctionBytes(supersedeId, publicFuncType, nonReturnType, supersedeDataType, supersedeOpcs)
  val supersedeFuncBytes: Array[Byte] = textualFunc("supersede", Seq(), supersedePara)

  // Set Swap
  val setSwapId: Short = 1
  val setSwapPara: Seq[String] = Seq("amountADesired", "amountBDesired") ++
                                 Seq("maker", "swapStatus", "valueFalse", "amountZero", "isValidAmountADesired", "isValidAmountBDesired",
                                     "bigIntType", "amountADesiredBigInt", "amountBDesiredBigInt", "valueK", "initLiquidityBigInt",
                                     "amountType", "initLiquidity", "minimumLiquidity", "isValidInitLiquidity", "liquidity",
                                     "maxLiquidity", "isValidLiquidity", "liquidityLeft", "valueTrue")
  val setSwapDataType: Array[Byte] = Array(DataType.Amount.id.toByte, DataType.Amount.id.toByte)
  val setSwapFunctionOpcs: Seq[Array[Byte]] = Seq(
    cdbvrGet ++ Array(makerStateVar.index, 2.toByte),
    assertCaller ++ Array(2.toByte),
    cdbvrGet ++ Array(swapStatusStateVar.index, 3.toByte),
    basicConstantGet ++ DataEntry(Array(0.toByte), DataType.Boolean).bytes ++ Array(4.toByte),
    assertEqual ++ Array(3.toByte, 4.toByte),
    basicConstantGet ++ DataEntry(Longs.toByteArray(0L), DataType.Amount).bytes ++ Array(5.toByte),
    compareGreater ++ Array(0.toByte, 5.toByte, 6.toByte),
    assertTrue ++ Array(6.toByte),
    compareGreater ++ Array(1.toByte, 5.toByte, 7.toByte),
    assertTrue ++ Array(7.toByte),
    cdbvMapValMinus ++ Array(tokenABalanceMap.index, 2.toByte, 0.toByte),
    cdbvMapValMinus ++ Array(tokenBBalanceMap.index, 2.toByte, 1.toByte),
    basicConstantGet ++ DataEntry(Array(DataType.BigInteger.id.toByte), DataType.DataTypeObj).bytes ++ Array(8.toByte),
    basicConvert ++ Array(0.toByte, 8.toByte, 9.toByte),
    basicConvert ++ Array(1.toByte, 8.toByte, 10.toByte),
    basicMultiply ++ Array(9.toByte, 10.toByte, 11.toByte),
    basicSqrtBigint ++ Array(11.toByte, 12.toByte),
    basicConstantGet ++ DataEntry(Array(DataType.Amount.id.toByte), DataType.DataTypeObj).bytes ++ Array(13.toByte),
    basicConvert ++ Array(12.toByte, 13.toByte, 14.toByte),
    cdbvrGet ++ Array(minimumLiquidityStateVar.index, 15.toByte),
    compareGreater ++ Array(14.toByte, 15.toByte, 16.toByte),
    assertTrue ++ Array(16.toByte),
    basicMinus ++ Array(14.toByte, 15.toByte, 17.toByte),
    cdbvrMapGet ++ Array(liquidityBalanceMap.index, 2.toByte, 18.toByte),
    compareGreater ++ Array(18.toByte, 14.toByte, 19.toByte),
    assertTrue ++ Array(19.toByte),
    cdbvMapValMinus ++ Array(liquidityBalanceMap.index, 2.toByte, 18.toByte),
    cdbvMapValAdd ++ Array(liquidityBalanceMap.index, 2.toByte, 17.toByte),
    basicMinus ++ Array(18.toByte, 14.toByte, 19.toByte),
    cdbvStateValAdd ++ Array(liquidityTokenLeftStateVar.index, 19.toByte),
    cdbvStateValAdd ++ Array(totalSupplyStateVar.index, 14.toByte),
    cdbvStateValAdd ++ Array(tokenAReservedStateVar.index, 0.toByte),
    cdbvStateValAdd ++ Array(tokenBReservedStateVar.index, 1.toByte),
    basicConstantGet ++ DataEntry(Array(1.toByte), DataType.Boolean).bytes ++ Array(20.toByte),
    cdbvSet ++ Array(swapStatusStateVar.index, 20.toByte)
  )
  lazy val setSwapFunc: Array[Byte] = getFunctionBytes(setSwapId, publicFuncType, nonReturnType, setSwapDataType, setSwapFunctionOpcs)
  val setSwapTextualBytes: Array[Byte] = textualFunc("setSwap", Seq(), setSwapPara)

  // Add Liquidity
  val addLiquidityId: Short = 2
  val addLiquidityPara: Seq[String] = Seq("amountADesired", "amountBDesired", "amountAMin", "amountBMin", "deadline") ++
                                      Seq("caller", "swapStatus", "currentTime", "isValidTime", "amountZero",
                                          "reserveA", "isValidReserveA", "reserveB", "isValidReserveB",
                                          "bigIntType", "reserveABigInt", "reserveBBigInt", "amountADesiredBigInt", "amountBDesiredBigInt",
                                          "mulA", "amountBGet", "mulB", "amountAGet", "amountType", "amountABigInt", "amountA", "isValidAmountA",
                                          "amountBBigInt", "amountB", "isValidAmountB", "totalSupply", "totalSupplyBigInt",
                                          "mulSupplyA", "mulSupplyB", "liquidityABigInt", "liquidityBBigInt", "liquidityBigInt",
                                          "liquidity", "liquidityLeft", "isValidLiquidity")
  val addLiquidityDataType: Array[Byte] = Array(DataType.Amount.id.toByte, DataType.Amount.id.toByte,
                                                DataType.Amount.id.toByte, DataType.Amount.id.toByte, DataType.Timestamp.id.toByte)
  val addLiquidityFunctionOpcs: Seq[Array[Byte]] = Seq(
    loadCaller ++ Array(5.toByte),
    cdbvrGet ++ Array(swapStatusStateVar.index, 6.toByte),
    assertTrue ++ Array(6.toByte),
    loadTimestamp ++ Array(7.toByte),
    compareGreaterEqual ++ Array(4.toByte, 7.toByte, 8.toByte),
    assertTrue ++ Array(8.toByte),
    basicConstantGet ++ DataEntry(Longs.toByteArray(0L), DataType.Amount).bytes ++ Array(9.toByte),
    cdbvrGetOrDefault ++ Array(tokenAReservedStateVar.index, 10.toByte),
    compareGreater ++ Array(10.toByte, 9.toByte, 11.toByte),
    assertTrue ++ Array(11.toByte),
    cdbvrGetOrDefault ++ Array(tokenBReservedStateVar.index, 12.toByte),
    compareGreater ++ Array(12.toByte, 9.toByte, 13.toByte),
    assertTrue ++ Array(13.toByte),
    basicConstantGet ++ DataEntry(Array(DataType.BigInteger.id.toByte), DataType.DataTypeObj).bytes ++ Array(14.toByte),
    basicConvert ++ Array(10.toByte, 14.toByte, 15.toByte),
    basicConvert ++ Array(12.toByte, 14.toByte, 16.toByte),
    basicConvert ++ Array(0.toByte, 14.toByte, 17.toByte),
    basicConvert ++ Array(1.toByte, 14.toByte, 18.toByte),
    basicMultiply ++ Array(17.toByte, 16.toByte, 19.toByte),
    basicDivide ++ Array(19.toByte, 15.toByte, 20.toByte),
    basicMultiply ++ Array(18.toByte, 15.toByte, 21.toByte),
    basicDivide ++ Array(21.toByte, 16.toByte, 22.toByte),
    basicConstantGet ++ DataEntry(Array(DataType.Amount.id.toByte), DataType.DataTypeObj).bytes ++ Array(23.toByte),
    basicMin ++ Array(20.toByte, 17.toByte, 24.toByte),
    basicConvert ++ Array(24.toByte, 23.toByte, 25.toByte),
    compareGreaterEqual ++ Array(25.toByte, 2.toByte, 26.toByte),
    assertTrue ++ Array(26.toByte),
    basicMin ++ Array(22.toByte, 18.toByte, 27.toByte),
    basicConvert ++ Array(27.toByte, 23.toByte, 28.toByte),
    compareGreaterEqual ++ Array(28.toByte, 3.toByte, 29.toByte),
    assertTrue ++ Array(29.toByte),
    cdbvMapValMinus ++ Array(tokenABalanceMap.index, 5.toByte, 25.toByte),
    cdbvMapValMinus ++ Array(tokenBBalanceMap.index, 5.toByte, 28.toByte),
    cdbvrGetOrDefault ++ Array(totalSupplyStateVar.index, 30.toByte),
    basicConvert ++ Array(30.toByte, 14.toByte, 31.toByte),
    basicMultiply ++ Array(24.toByte, 31.toByte, 32.toByte),
    basicMultiply ++ Array(27.toByte, 31.toByte, 33.toByte),
    basicDivide ++ Array(32.toByte, 15.toByte, 34.toByte),
    basicDivide ++ Array(33.toByte, 16.toByte, 35.toByte),
    basicMin ++ Array(34.toByte, 35.toByte, 36.toByte),
    basicConvert ++ Array(36.toByte, 23.toByte, 37.toByte),
    cdbvrGetOrDefault ++ Array(liquidityTokenLeftStateVar.index, 38.toByte),
    compareGreaterEqual ++ Array(38.toByte, 37.toByte, 39.toByte),
    assertTrue ++ Array(39.toByte),
    cdbvStateValMinus ++ Array(liquidityTokenLeftStateVar.index, 37.toByte),
    cdbvStateValAdd ++ Array(totalSupplyStateVar.index, 37.toByte),
    cdbvStateValAdd ++ Array(tokenAReservedStateVar.index, 25.toByte),
    cdbvStateValAdd ++ Array(tokenBReservedStateVar.index, 28.toByte),
    cdbvMapValAdd ++ Array(liquidityBalanceMap.index, 5.toByte, 37.toByte)
  )
  lazy val addLiquidityFunc: Array[Byte] = getFunctionBytes(addLiquidityId, publicFuncType, nonReturnType,
                                                            addLiquidityDataType, addLiquidityFunctionOpcs)
  val addLiquidityTextualBytes: Array[Byte] = textualFunc("addLiquidity", Seq(), addLiquidityPara)

  // Remove Liquidity
  val removeLiquidityId: Short = 2
  val removeLiquidityPara: Seq[String] = Seq("liquidity", "amountAMin", "amountBMin", "deadline") ++
                                         Seq("caller", "swapStatus", "currentTime", "isValidTime", "reserveA", "reserveB",
                                             "bigIntType", "liquidityBigInt", "reserveABigInt", "reserveBBigInt",
                                             "totalSupply", "amountZero", "isValidTotalSupply", "totalSupplyBigInt",
                                             "mulA", "amountABigInt", "mulB", "amountBBigInt", "amountType", "amountA", "amountB",
                                             "isValidAmountA", "isValidAmountB")
  val removeLiquidityDataType: Array[Byte] = Array(DataType.Amount.id.toByte, DataType.Amount.id.toByte,
                                                   DataType.Amount.id.toByte, DataType.Timestamp.id.toByte)
  val removeLiquidityFunctionOpcs: Seq[Array[Byte]] = Seq(
    loadCaller ++ Array(4.toByte),
    cdbvrGet ++ Array(swapStatusStateVar.index, 5.toByte),
    assertTrue ++ Array(5.toByte),
    loadTimestamp ++ Array(6.toByte),
    compareGreaterEqual ++ Array(3.toByte, 6.toByte, 7.toByte),
    assertTrue ++ Array(7.toByte),
    cdbvMapValMinus ++ Array(liquidityBalanceMap.index, 4.toByte, 0.toByte),
    cdbvrGetOrDefault ++ Array(tokenAReservedStateVar.index, 8.toByte),
    cdbvrGetOrDefault ++ Array(tokenBReservedStateVar.index, 9.toByte),
    basicConstantGet ++ DataEntry(Array(DataType.BigInteger.id.toByte), DataType.DataTypeObj).bytes ++ Array(10.toByte),
    basicConvert ++ Array(0.toByte, 10.toByte, 11.toByte),
    basicConvert ++ Array(8.toByte, 10.toByte, 12.toByte),
    basicConvert ++ Array(9.toByte, 10.toByte, 13.toByte),
    cdbvrGetOrDefault ++ Array(totalSupplyStateVar.index, 14.toByte),
    basicConstantGet ++ DataEntry(Longs.toByteArray(0L), DataType.Amount).bytes ++ Array(15.toByte),
    compareGreater ++ Array(14.toByte, 15.toByte, 16.toByte),
    assertTrue ++ Array(16.toByte),
    basicConvert ++ Array(14.toByte, 10.toByte, 17.toByte),
    basicMultiply ++ Array(11.toByte, 12.toByte, 18.toByte),
    basicDivide ++ Array(18.toByte, 17.toByte, 19.toByte),
    basicMultiply ++ Array(11.toByte, 13.toByte, 20.toByte),
    basicDivide ++ Array(20.toByte, 17.toByte, 21.toByte),
    basicConstantGet ++ DataEntry(Array(DataType.Amount.id.toByte), DataType.DataTypeObj).bytes ++ Array(22.toByte),
    basicConvert ++ Array(19.toByte, 22.toByte, 23.toByte),
    basicConvert ++ Array(21.toByte, 22.toByte, 24.toByte),
    compareGreaterEqual ++ Array(23.toByte, 1.toByte, 25.toByte),
    assertTrue ++ Array(25.toByte),
    compareGreaterEqual ++ Array(24.toByte, 2.toByte, 26.toByte),
    assertTrue ++ Array(26.toByte),
    cdbvStateValMinus ++ Array(totalSupplyStateVar.index, 0.toByte),
    cdbvStateValMinus ++ Array(tokenAReservedStateVar.index, 23.toByte),
    cdbvStateValMinus ++ Array(tokenBReservedStateVar.index, 24.toByte),
    cdbvMapValAdd ++ Array(tokenABalanceMap.index, 4.toByte, 23.toByte),
    cdbvMapValAdd ++ Array(tokenBBalanceMap.index, 4.toByte, 24.toByte),
    cdbvStateValAdd ++ Array(liquidityTokenLeftStateVar.index, 0.toByte)
  )
  lazy val removeLiquidityFunc: Array[Byte] = getFunctionBytes(removeLiquidityId, publicFuncType, nonReturnType,
                                                               removeLiquidityDataType, removeLiquidityFunctionOpcs)
  val removeLiquidityTextualBytes: Array[Byte] = textualFunc("removeLiquidity", Seq(), removeLiquidityPara)

  // Common Swap Code
  val commonSwapPara: Seq[String] = Seq("caller", "swapStatus", "currentTime", "isValidTime")
  val swapDataType: Array[Byte] = Array(DataType.Amount.id.toByte, DataType.Amount.id.toByte, DataType.Timestamp.id.toByte)
  val commonSwapFunctionOpcs: Seq[Array[Byte]] = Seq(
    loadCaller ++ Array(3.toByte),
    cdbvrGet ++ Array(swapStatusStateVar.index, 4.toByte),
    assertTrue ++ Array(4.toByte),
    loadTimestamp ++ Array(5.toByte),
    compareGreaterEqual ++ Array(2.toByte, 5.toByte, 6.toByte),
    assertTrue ++ Array(6.toByte),
  )

  val swapTokenForExactBaseTokenId: Short = 3
  val swapTokenForExactBaseTokenPara: Seq[String] = Seq("amountOut", "amountInMax", "deadline") ++
                                                    commonSwapPara ++
                                                    Seq("amountZero", "isValidAmountOut", "reserveA", "reserveB",
                                                        "bigIntType", "amountOutBigInt", "reserveABigInt", "reserveBBigInt",
                                                        "value1000", "mulValue", "numerator", "value997", "subValue", "denominator",
                                                        "valueOne", "amountInBigIntPre", "amountInBigInt", "amountType",
                                                        "amountIn", "isValidAmountIn", "updatedReserveA", "amountInWithOutFee",
                                                        "updatedReserveB", "updatedK", "KValue", "valueSquare", "oldK", "isValidUpdatedK"
                                                    )
  val swapTokenForExactBaseTokenFunctionOpcs: Seq[Array[Byte]] = commonSwapFunctionOpcs ++ Seq(
    basicConstantGet ++ DataEntry(Longs.toByteArray(0L), DataType.Amount).bytes ++ Array(7.toByte),
    compareGreater ++ Array(0.toByte, 7.toByte, 8.toByte),
    assertTrue ++ Array(8.toByte),
    cdbvrGetOrDefault ++ Array(tokenAReservedStateVar.index, 9.toByte),
    cdbvrGetOrDefault ++ Array(tokenBReservedStateVar.index, 10.toByte),
    basicConstantGet ++ DataEntry(Array(DataType.BigInteger.id.toByte), DataType.DataTypeObj).bytes ++ Array(11.toByte),
    basicConvert ++ Array(0.toByte, 11.toByte, 12.toByte),
    basicConvert ++ Array(9.toByte, 11.toByte, 13.toByte),
    basicConvert ++ Array(10.toByte, 11.toByte, 14.toByte),
    basicConstantGet ++ DataEntry(BigInt(1000).toByteArray, DataType.BigInteger).bytes ++ Array(15.toByte),
    basicMultiply ++ Array(14.toByte, 15.toByte, 16.toByte),
    basicMultiply ++ Array(16.toByte, 12.toByte, 17.toByte),
    basicConstantGet ++ DataEntry(BigInt(997).toByteArray, DataType.BigInteger).bytes ++ Array(18.toByte),
    basicMinus ++ Array(13.toByte, 12.toByte, 19.toByte),
    basicMultiply ++ Array(19.toByte, 18.toByte, 20.toByte),
    basicConstantGet ++ DataEntry(BigInt(1).toByteArray, DataType.BigInteger).bytes ++ Array(21.toByte),
    basicDivide ++ Array(17.toByte, 20.toByte, 22.toByte),
    basicAdd ++ Array(22.toByte, 21.toByte, 23.toByte),
    basicConstantGet ++ DataEntry(Array(DataType.Amount.id.toByte), DataType.DataTypeObj).bytes ++ Array(24.toByte),
    basicConvert ++ Array(23.toByte, 24.toByte, 25.toByte),
    compareGreaterEqual ++ Array(25.toByte, 1.toByte, 26.toByte),
    assertTrue ++ Array(26.toByte),
    basicMultiply ++ Array(19.toByte, 15.toByte, 27.toByte),
    basicMultiply ++ Array(23.toByte, 18.toByte, 28.toByte),
    basicAdd ++ Array(16.toByte, 28.toByte, 29.toByte),
    basicMultiply ++ Array(27.toByte, 29.toByte, 30.toByte),
    basicMultiply ++ Array(13.toByte, 14.toByte, 31.toByte),
    basicMultiply ++ Array(15.toByte, 15.toByte, 32.toByte),
    basicMultiply ++ Array(31.toByte, 32.toByte, 33.toByte),
    compareGreaterEqual ++ Array(30.toByte, 33.toByte, 34.toByte),
    assertTrue ++ Array(34.toByte),
    // minus may put directly after load reserved token value A and B
    cdbvStateValMinus ++ Array(tokenAReservedStateVar.index, 0.toByte),
    cdbvMapValMinus ++ Array(tokenBBalanceMap.index, 3.toByte, 25.toByte),
    cdbvStateValAdd ++ Array(tokenBReservedStateVar.index, 25.toByte),
    cdbvMapValAdd ++ Array(tokenBBalanceMap.index, 3.toByte, 0.toByte)
  )
  lazy val swapTokenForExactBaseTokenFunc: Array[Byte] = getFunctionBytes(swapTokenForExactBaseTokenId, publicFuncType, nonReturnType,
                                                                          swapDataType, swapTokenForExactBaseTokenFunctionOpcs)
  val swapTokenForExactBaseTokenTextualBytes: Array[Byte] = textualFunc("swapTokenForExactBaseToken", Seq(), swapTokenForExactBaseTokenPara)

  
  // Textual

}