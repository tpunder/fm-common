package fm.common.rich

import org.scalatest.FunSuite
import org.scalatest.Matchers

import com.ctc.wstx.stax.WstxInputFactory
import org.codehaus.stax2.XMLStreamReader2
import javax.xml.stream.XMLInputFactory
import javax.xml.stream.XMLStreamConstants._
import javax.xml.stream.XMLStreamException
import java.io.StringReader
import fm.common.rich.RichXMLStreamReader2.toRichXMLStreamReader2

final class TestRichXMLStreamReader2 extends FunSuite with Matchers {
  
  test("seekToRootElement()") {
    val sr = createSR()
    sr.seekToRootElement("root")
    sr.getDepth should equal (1)
    sr.getEventType should equal (START_ELEMENT)
    sr.getLocalName() should equal ("root")
  }
  
  test("seekToSiblingElement - Exception") {
    var sr = createSR()
    sr.seekToRootElement()
    intercept[XMLStreamException] { sr.seekToSiblingElement("header") }
  }
  
  test("seekToChildElement()") {
    var sr = createSR()
    sr.seekToRootElement()
    sr.seekToChildElement("items")
  }
  
  test("seekToChildElement() - Exception") {
    var sr = createSR()
    sr.seekToRootElement()
    intercept[XMLStreamException] { sr.seekToChildElement("items_foo") }
  }
  
  test("Simple Document Traversing") {
    var sr = createSR()
    sr.seekToRootElement()
    sr.seekToChildElement("items")
    sr.seekToChildElement("item")
    sr.readChildElementText("name") should equal ("Item 1 Name")
    sr.seekToSiblingElement("item")
    sr.seekToEndOfParentElement()
    sr.seekToSiblingElement("trailer")
    sr.seekToChildElement("name")
    sr.readElementText() should equal ("Trailer Name")
  }
  
  test("foreach - root/items/item") {
    var sr = createSR()
    val builder = Vector.newBuilder[String]
    
    sr.foreach("root/items/item") {
      builder += sr.readChildElementText("name")
    }
    
    builder.result should equal (Vector("Item 1 Name", "Item 2 Name"))
  }
  
  test("foreach - root/items/item/name") {
    var sr = createSR()
    val builder = Vector.newBuilder[String]
    
    sr.foreach("root/items/item/name") {
      builder += sr.readElementText()
    }
    
    builder.result should equal (Vector("Item 1 Name", "Item 2 Name"))
  }
  
  private def createSR(): XMLStreamReader2 = {
    val inputFactory = new WstxInputFactory()
    inputFactory.setProperty(XMLInputFactory.SUPPORT_DTD, false)
    inputFactory.configureForSpeed()
    inputFactory.createXMLStreamReader(new StringReader(xml)).asInstanceOf[XMLStreamReader2]
  }
  
val xml = """
<?xml version='1.0' encoding='UTF-8'?>
<root>
  <header>
    <name>Header Name</name>
  </header>
  <items>
    <item idx="1">
      <name>Item 1 Name</name>
    </item>
    <item idx="2">
      <name>Item 2 Name</name>
    </item>
  </items>
  <trailer>
    <name>Trailer Name</name>
  </trailer>
</root>
""".trim
}