/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.brokering.admin;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;
import javax.xml.bind.JAXBException;

/**
 *
 * @author Chris
 */
@Path("/setup")
public class SetUpRespurce {
 
    private final File statsFile = new File("stats.xml");
    
    @Context
    private UriInfo context;

    public SetUpRespurce() {
        
    }
    
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String init() {
        
        Stats stats = new Stats();
        List<Stat> statList = new ArrayList<>();
        
        Stat stat = new Stat();
        stat.setCompanyName("Apple");
        stat.setCompanySymobol("AAPL");
        Buy buy = new Buy();
        List<Buy> boughtList = new ArrayList<>();
        boughtList.add(buy);
        stat.bought = boughtList;
        statList.add(stat);
        
        stat = new Stat();
        stat.setCompanyName("Google");
        stat.setCompanySymobol("GOOG");
        buy = new Buy();
        boughtList = new ArrayList<>();
        boughtList.add(buy);
        stat.bought = boughtList;
        statList.add(stat);
        
        stat = new Stat();
        stat.setCompanyName("Intel");
        stat.setCompanySymobol("INTX");
        buy = new Buy();
        boughtList = new ArrayList<>();
        boughtList.add(buy);
        stat.bought = boughtList;
        statList.add(stat);
        
        stat = new Stat();
        stat.setCompanyName("Nvidia");
        stat.setCompanySymobol("NVDA");
        buy = new Buy();
        boughtList = new ArrayList<>();
        boughtList.add(buy);
        stat.bought = boughtList;
        statList.add(stat);
        
        
        stats.stats = statList;
        
        
        marshal(stats);
        return "hello";
    }
    private void marshal(Stats stats) {
        try {
            javax.xml.bind.JAXBContext jaxbCtx = javax.xml.bind.JAXBContext.newInstance(Stats.class);
            javax.xml.bind.Marshaller marshaller = jaxbCtx.createMarshaller();
            marshaller.setProperty(javax.xml.bind.Marshaller.JAXB_ENCODING, "UTF-8"); //NOI18N
            marshaller.setProperty(javax.xml.bind.Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
            marshaller.marshal(stats, statsFile);
        } catch (JAXBException e) {
            System.out.println(e);
        }
    }
    
}
