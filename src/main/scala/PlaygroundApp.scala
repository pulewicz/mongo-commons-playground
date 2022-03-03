import com.avsystem.commons.mongo.{
  BsonReaderInput,
  BsonValueInput,
  BsonValueOutput,
  BsonWriterOutput
}
import com.avsystem.commons.serialization.json.JsonStringInput
import com.avsystem.commons.serialization.{GenCodec, HasGenCodec, flatten}
import org.bson.{BsonDocument, BsonDocumentReader, BsonDocumentWriter}

import scala.util.control.NonFatal

object HierarchyTypes {
  @flatten("type")
  sealed trait TestType

  case class TestSubType1(x: Int, y: Int) extends TestType

  case class TestSubType2(z: String) extends TestType

  object TestType extends HasGenCodec[TestType]
}

object SimpleTypes {
  case class TypeWithBigDecimal(x: BigDecimal, y: BigDecimal)

  object TypeWithBigDecimal extends HasGenCodec[TypeWithBigDecimal]
}

object PlaygroundApp {
  import HierarchyTypes._
  import SimpleTypes._

  def testJsonStringDecoding(): Unit = {
    println("Testing Json decoding...")

    val jsonString1 = """{"x":10,"y":20,"type":"TestSubType1"}"""
    val jsonString2 = """{"z":"Test","type":"TestSubType2"}"""

    val decoded1 = JsonStringInput.read[TestType](jsonString1)
    val decoded2 = JsonStringInput.read[TestType](jsonString2)

    println(decoded1)
    println(decoded2)
  }

  def testBsonValueDecoding(): Unit = {
    println("Testing Bson value decoding...")

    val bsonDocument1 =
      BsonDocument.parse("""{"x": 10, "y": 20, "type": "TestSubType1"}""")
    val bsonDocument2 =
      BsonDocument.parse("""{"z": "Test", "type": "TestSubType2"}""")

    val decoded1 = BsonValueInput.read[TestType](bsonDocument1)
    val decoded2 = BsonValueInput.read[TestType](bsonDocument2)

    println(decoded1)
    println(decoded2)
  }

  def testBsonReaderDecoding(): Unit = {
    println("Testing Bson reader decoding...")

    val bsonDocument = {
      val document = new BsonDocument()
      val writer = new BsonDocumentWriter(document)
      val output = new BsonWriterOutput(writer)

      val objOutput = output.writeObject()
      objOutput.writeField("x").writeInt(10)
      objOutput.writeField("y").writeInt(20)
      objOutput.writeField("type").writeString("TestSubType1")

      document
    }

    val reader = new BsonDocumentReader(bsonDocument)
    val input = new BsonReaderInput(reader)

    try {
      val decoded = GenCodec.read[TestType](input)
      println(decoded)
    } catch {
      case NonFatal(e) => println(e)
    }
  }

  def testBsonBigDecimalEncoding(): Unit = {
    println("Testing Bson big decimal encoding")

    val obj = TypeWithBigDecimal(x = 103234.42343, y = 12344.423)

    val bsonDocument = new BsonDocument()
    val writer = new BsonDocumentWriter(bsonDocument)
    val output = new BsonWriterOutput(writer)

    GenCodec.write(output, obj)

    println(bsonDocument)
  }

  def main(args: Array[String]): Unit = {
    testJsonStringDecoding()
    testBsonValueDecoding()
    testBsonReaderDecoding()
    testBsonBigDecimalEncoding()
  }
}
