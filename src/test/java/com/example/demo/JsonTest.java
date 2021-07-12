package com.example.demo;

import com.example.demo.jsonData.Detail;
import com.example.demo.jsonData.Master;
import com.example.demo.jsonData.PartStock;
import com.example.demo.jsonData.PartStockLine;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.ser.ToXmlGenerator;
import org.junit.jupiter.api.Test;

import java.util.Date;

class JsonTest {
    @Test
    void json() {
        String name="master\" name";
        Master master = new Master();
        master.setId("1");
        master.setName(name);
        Detail detail = new Detail();
        detail.setId("#1");
        detail.setName("Super Clue");
        master.getDetails().add(detail);
        master.setMyDate(null);

        XmlMapper xmlMapper = new XmlMapper();
        try {
            //#1
//            xmlMapper.configure(SerializationFeature.WRAP_ROOT_VALUE, true);
            //xmlMapper.writer().with(CharacterEscapes.ESCAPE_NONE);
            xmlMapper.enable(SerializationFeature.INDENT_OUTPUT);
            xmlMapper.configure(ToXmlGenerator.Feature.WRITE_XML_DECLARATION, true);
//            String xml = xmlMapper.writeValueAsString(master);
            String xml = xmlMapper.writer().withRootName("Issue").writeValueAsString(master);
            System.out.println(xml);
            //#2
            PartStock stock=new PartStock();
            stock.setCustomerID(300255);
            stock.setTimeStamp(new Date());

            PartStockLine item = new PartStockLine();
            item.setArticle("art");
            item.setLineNo(1);
            item.setName("art name");
            item.setQty(33);
            item.setUpc("2030450600");
//            List<PartStockList> stockList = new ArrayList<>();
//            stockList.add(item);
            stock.getStockLines().add(item);
            System.out.println(xmlMapper.writeValueAsString(stock));
            //deserialize
            xml=xmlMapper.writeValueAsString(stock);
            PartStock st = xmlMapper.readValue(xml, PartStock.class);
            System.out.println("stop");
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

    }
}