package vsys.blockchain.contract

import com.google.common.primitives.{Ints, Longs}
import vsys.blockchain.contract.ContractGen._
import vsys.blockchain.state._
import vsys.utils.serialization.Deser

object ContractNonFungibleV2 {
  lazy val contractNFTWhitelist: Contract = Contract.buildContract(Deser.serilizeString("vdds"), Ints.toByteArray(2),
    Seq(),
    Seq(),
    Seq(),
    Seq(),
    Seq()
  ).explicitGet()

  lazy val contractNFTBlacklist: Contract = Contract.buildContract(Deser.serilizeString("vdds"), Ints.toByteArray(2),
    Seq(),
    Seq(),
    Seq(),
    Seq(),
    Seq()
  ).explicitGet()

  // StateVar
  val stateVarName = List("issuer", "maker")
  val issuerStateVar: StateVar = StateVar(0.toByte, DataType.Address.id.toByte)
  val makerStateVar: StateVar = StateVar(1.toByte, DataType.Address.id.toByte)
  lazy val stateVarTextual: Array[Byte] = Deser.serializeArrays(stateVarName.map(x => Deser.serilizeString(x)))

  // StateMap
  val stateMapWhitelist = List("whitelist", "userAccount", "isInList")
  val stateMapBlacklist = List("blacklist", "userAccount", "isInList")
  val listMap: StateMap = StateMap(0.toByte, DataType.Account.id.toByte, DataType.Boolean.id.toByte)

  // initTrigger
  val initId: Short = 0
  val initPara: Seq[String] = Seq(
    "signer")
  val initDataType: Array[Byte] = Array()
  val initOpcs: Seq[Array[Byte]] = Seq(
    loadSigner ++ Array(0.toByte),
    cdbvSet ++ Array(issuerStateVar.index, 0.toByte),
    cdbvSet ++ Array(makerStateVar.index, 0.toByte))
  lazy val initFunc: Array[Byte] = getFunctionBytes(initId, onInitTriggerType, nonReturnType, initDataType, initOpcs)
  lazy val initFuncBytes: Array[Byte] = textualFunc("init", Seq(), initPara)

  // Functions
  // Supersede
  val supersedeId: Short = 0
  val supersedePara: Seq[String] = Seq("newIssuer",
    "maker")
  val supersedeDataType: Array[Byte] = Array(DataType.Account.id.toByte)
  val supersedeOpcs: Seq[Array[Byte]] =  Seq(
    cdbvrGet ++ Array(makerStateVar.index, 1.toByte),
    assertSigner ++ Array(1.toByte),
    cdbvSet ++ Array(issuerStateVar.index, 0.toByte))
  lazy val supersedeFunc: Array[Byte] = getFunctionBytes(supersedeId, publicFuncType, nonReturnType, supersedeDataType, supersedeOpcs)
  val supersedeFuncBytes: Array[Byte] = textualFunc("supersede", Seq(), supersedePara)

  // Issue
  val issueId: Short = 1
  val issuePara: Seq[String] = Seq("tokenDescription",
    "issuer", "amount", "tokens")
  val issueDataType: Array[Byte] = Array(DataType.ShortText.id.toByte)
  val issueOpcs: Seq[Array[Byte]] = Seq(
    cdbvrGet ++ Array(issuerStateVar.index, 1.toByte),
    assertCaller ++ Array(1.toByte),
    basicConstantGet ++ DataEntry(Longs.toByteArray(1), DataType.Amount).bytes ++ Array(2.toByte),
    tdbNewToken ++ Array(2.toByte, 2.toByte, 0.toByte),
    loadLastTokenIndex ++ Array(3.toByte),
    tdbaDeposit ++ Array(1.toByte, 2.toByte, 3.toByte))
  lazy val issueFunc: Array[Byte] = getFunctionBytes(issueId, publicFuncType, nonReturnType, issueDataType, issueOpcs)
  val issueFuncBytes: Array[Byte] = textualFunc("issue", Seq(), issuePara)

  // update list
  val updateListId: Short = 2
  val updateListPara: Seq[String] = Seq("userAccount", "value",
    "issuer")
  val updateListDataType: Array[Byte] = Array(DataType.Account.id.toByte, DataType.Boolean.id.toByte)
  val updateListOpcs: Seq[Array[Byte]] = Seq(
    cdbvrGet ++ Array(issuerStateVar.index, 2.toByte),
    assertCaller ++ Array(2.toByte),
    cdbvMapSet ++ Array(listMap.index, 0.toByte, 1.toByte)
  )
  lazy val updateListFunc: Array[Byte] = getFunctionBytes(updateListId, publicFuncType, nonReturnType, updateListDataType, updateListOpcs)
  val updateListFuncBytes: Array[Byte] = textualFunc("updateList", Seq(), updateListPara)

  // send
  val sendId: Short = 3
  val sendPara: Seq[String] = Seq("recipient", "tokenIndex",
    "caller", "amount", "value", "isSenderInList", "isRecipientInList")
  val sendDataType: Array[Byte] = Array(DataType.Account.id.toByte, DataType.Int32.id.toByte)

  // whitelist
  val sendWhitelistOpcs: Seq[Array[Byte]] = Seq(
    loadCaller ++ Array(2.toByte),
    basicConstantGet ++ DataEntry(Longs.toByteArray(1), DataType.Amount).bytes ++ Array(3.toByte)
  ) ++ whitelistCheck(2.toByte, 0.toByte) ++ Seq(
    tdbaTransfer ++ Array(2.toByte, 0.toByte, 3.toByte, 1.toByte)
  )
  lazy val sendWhitelistFunc: Array[Byte] = getFunctionBytes(sendId, publicFuncType, nonReturnType, sendDataType, sendWhitelistOpcs)
  val sendWhitelistFuncBytes: Array[Byte] = textualFunc("send", Seq(), sendPara)

  // blacklist
  val sendBlacklistOpcs: Seq[Array[Byte]] = Seq(
    loadCaller ++ Array(2.toByte),
    basicConstantGet ++ DataEntry(Longs.toByteArray(1), DataType.Amount).bytes ++ Array(3.toByte)
  ) ++ blacklistCheck(2.toByte, 0.toByte) ++ Seq(
    tdbaTransfer ++ Array(2.toByte, 0.toByte, 3.toByte, 1.toByte)
  )
  lazy val sendBlacklistFunc: Array[Byte] = getFunctionBytes(sendId, publicFuncType, nonReturnType, sendDataType, sendBlacklistOpcs)
  val sendBlacklistFuncBytes: Array[Byte] = textualFunc("send", Seq(), sendPara)

  private def whitelistCheck(sender: Byte, recipient: Byte): Seq[Array[Byte]] =
    Seq(
      basicConstantGet ++ DataEntry(Array(1.toByte), DataType.Boolean).bytes ++ Array(4.toByte),
      cdbvrMapGetOrDefault ++ Array(listMap.index, sender, 5.toByte),
      assertEqual ++ Array(5.toByte, 4.toByte),
      cdbvrMapGetOrDefault ++ Array(listMap.index, recipient, 6.toByte),
      assertEqual ++ Array(6.toByte, 4.toByte),
    )

  private def blacklistCheck(sender: Byte, recipient: Byte): Seq[Array[Byte]] =
    Seq(
      basicConstantGet ++ DataEntry(Array(0.toByte), DataType.Boolean).bytes ++ Array(4.toByte),
      cdbvrMapGetOrDefault ++ Array(listMap.index, sender, 5.toByte),
      assertEqual ++ Array(5.toByte, 4.toByte),
      cdbvrMapGetOrDefault ++ Array(listMap.index, recipient, 6.toByte),
      assertEqual ++ Array(6.toByte, 4.toByte)
    )

}
