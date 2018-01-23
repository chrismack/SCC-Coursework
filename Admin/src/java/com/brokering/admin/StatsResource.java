/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.brokering.admin;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ws.rs.BeanParam;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import static javax.ws.rs.HttpMethod.POST;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.xml.bind.JAXBException;

/**
 *
 * @author Chris
 */
@Path("/stats")
public class StatsResource {
    
    private File statsFile = new File("stats.xml");
    
    @Context
    private UriInfo context;

    public StatsResource() {
        
    }
    
    @GET
    @Produces(MediaType.APPLICATION_XML)
    public Stats getStats() {
        return unmarshal();
    }
    
    @GET
    @Path("/{symbol}")
    @Produces(MediaType.APPLICATION_XML)
    public Stat getStat(@PathParam("symbol") String symbol) {
        Stats stats = unmarshal();
        for(Stat stat : stats.getStats()) {
            if(stat.getCompanySymobol().equalsIgnoreCase(symbol)) {
                return stat;
            }
        }
        return null;
    }
    
    @POST
    @Path("/{symbol}")
    @Consumes(MediaType.TEXT_XML)
    public Response updateStat(@PathParam("symbol") String sym,  final Buy content) {
        Stats stats = unmarshal();
        boolean marshal = false;
        for(Stat stat : stats.getStats()) {
            if(stat.getCompanySymobol().equalsIgnoreCase(sym)) {
                stat.getBought().add(content);
                marshal = true;
            }
        }
        
        if(marshal) {
            marshal(stats);
        }
        return Response.accepted().build();
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
    
    private Stats unmarshal() {
        Stats stats = null;
        try {
            javax.xml.bind.JAXBContext jaxbCtx = javax.xml.bind.JAXBContext.newInstance(Stats.class);
            javax.xml.bind.Unmarshaller unmarshaller = jaxbCtx.createUnmarshaller();
            stats = (Stats) unmarshaller.unmarshal(statsFile); //NOI18N
        } catch (javax.xml.bind.JAXBException ex) {
            // XXXTODO Handle exception
            java.util.logging.Logger.getLogger("global").log(java.util.logging.Level.SEVERE, null, ex); //NOI18N
        }
        return stats;
    }
    
    
}
