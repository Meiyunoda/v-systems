package vsys.api.http.spos

import javax.ws.rs.Path
import akka.http.scaladsl.server.Route
import io.swagger.annotations._
import play.api.libs.json.{JsArray, Json}
import vsys.account.Address
import vsys.api.http.{ApiRoute, CommonApiFunctions, InvalidAddress, InvalidSlotId}
import vsys.blockchain.consensus.{PoSCalc, SPoSCalc}
import vsys.blockchain.history.History
import vsys.blockchain.state.reader.StateReader
import vsys.settings.{FunctionalitySettings, RestAPISettings}

@Path("/consensus")
@Api(value = "/consensus")
case class SposConsensusApiRoute(
    settings: RestAPISettings,
    state: StateReader,
    history: History,
    fs:FunctionalitySettings) extends ApiRoute with CommonApiFunctions {

  override val route: Route =
    pathPrefix("consensus") {
      algo ~ allSlotsInfo ~ mintingAverageBalance ~ mintingAverageBalanceId ~ mintTime ~ mintTimeId ~ generatingBalance
    }

  @Path("/generatingBalance/{address}")
  @ApiOperation(value = "Generating balance", notes = "Get Account's generating balance(the same as balance atm)", httpMethod = "GET")
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "address", value = "Address", required = true, dataType = "string", paramType = "path")
  ))
  @ApiResponses(Array(
    new ApiResponse(code = 200, message = "Json with error or response like {\"address\": \"your address\",\"balance\": \"your balance\"}")
  ))
  def generatingBalance: Route = (path("generatingBalance" / Segment) & get) { address =>
    Address.fromString(address) match {
      case Left(_) => complete(InvalidAddress)
      case Right(account) =>
        complete(Json.obj(
          "address" -> account.address,
          "balance" -> PoSCalc.generatingBalance(state, fs, account, state.height)))
    }
  }

  @Path("/mintingAverageBalance/{address}")
  @ApiOperation(value = "Minting average balance", notes = "Get the **minting average balance**(MAB) of an `address`", httpMethod = "GET")
  @ApiResponses(Array(
    new ApiResponse(code = 200, message = "Json response of an address's MAB or error")
  ))
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "address", value = "Address", required = true, dataType = "string", paramType = "path")
  ))
  def mintingAverageBalance: Route = (path("mintingAverageBalance" / Segment) & get) { address =>
    Address.fromString(address) match {
      case Left(_) => complete(InvalidAddress)
      case Right(account) =>
        complete(Json.obj(
          "address" -> account.address,
          "mintingAverageBalance" -> SPoSCalc.mintingBalance(state, fs, account, state.height),
          "height" -> state.height))
    }
  }

  @Path("/allSlotsInfo")
  @ApiResponses(Array(
    new ApiResponse(code = 200, message = "Json response of all slots details or error")
  ))
  def allSlotsInfo: Route = (path("allSlotsInfo") & get) {
    val h = state.height
    val ret = Json.arr(Json.obj("height" -> h)) ++ JsArray(
      (0 until fs.numOfSlots).map{
        f => state.slotAddress(f) match {
          case None => Json.obj(
            "slotId"-> f,
            "address" -> "None",
            "mintingAverageBalance" -> 0)
          case Some(address) => Address.fromString(address) match {
            case Left(_) => Json.obj(
              "slotId"-> f,
              "address" -> "Error address",
              "mintingAverageBalance" -> 0)
            case Right(account) =>
              Json.obj(
                "slotId"-> f,
                "address" -> account.address,
                "mintingAverageBalance" -> SPoSCalc.mintingBalance(state, fs, account, h))
          }
        }
      }
    )
    complete(ret)
  }

  @Path("/slotInfo/{slotId}")
  @ApiOperation(value = "Minting average balance with slot ID", notes = "Get the supernode's minting average balance by specified `slotId`", httpMethod = "GET")
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "slotId", value = "Slot Id", required = true, dataType = "integer", paramType = "path")
  ))
  @ApiResponses(Array(
    new ApiResponse(code = 200, message = "Json response of the minting average balance of a specified slot info or error")
  ))
  def mintingAverageBalanceId: Route = (path("slotInfo" / IntNumber) & get) { slotId =>
    state.slotAddress(slotId) match {
      case None if slotId >= 0 && slotId < fs.numOfSlots =>
        complete(Json.obj(
          "slotId"-> slotId,
          "address" -> "None",
          "mintingAverageBalance" -> 0,
          "height" -> state.height))
      case Some(address) =>
        Address.fromString(address) match {
          case Left(_) => complete(InvalidAddress)
          case Right(account) =>
            complete(Json.obj(
              "slotId"-> slotId,
              "address" -> account.address,
              "mintingAverageBalance" -> SPoSCalc.mintingBalance(state, fs, account, state.height),
              "height" -> state.height))
        }
      case _ => complete(InvalidSlotId(slotId))
    }
  }

  @Path("/mintTime/{blockId}")
  @ApiOperation(value = "Mint time", notes = "Mint time of a block with specified `blockId`", httpMethod = "GET", response = classOf[Long])
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "blockId", value = "Block id ", required = true, dataType = "string", paramType = "path")
  ))
  def mintTimeId: Route = (path("mintTime" / Segment) & get) { encodedSignature =>
    withBlock(history, encodedSignature) { block =>
      complete(Json.obj("mintTime" -> block.consensusData.mintTime))
    }
  }

  @Path("/mintTime")
  @ApiOperation(value = "Mint time last", notes = "Mint time of a last block", httpMethod = "GET", response = classOf[Long])
  def mintTime: Route = (path("mintTime") & get) {
    complete(Json.obj("mintTime" -> history.lastBlock.get.consensusData.mintTime))
  }

  @Path("/algo")
  @ApiOperation(value = "Consensus algo", notes = "Shows which **consensus algo** are being used", httpMethod = "GET")
  @ApiResponses(Array(
    new ApiResponse(code = 200, message = "Json with error or response like {\"consensusAlgo\": \"current adopting algo\"}")
  ))
  def algo: Route = (path("algo") & get) {
    complete(Json.obj("consensusAlgo" -> "supernode-proof-of-stake (SPoS)"))
  }
}
