<%-- 
    Document   : index
    Created on : 07-Dec-2017, 23:11:07
    Author     : Chris
--%>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@page import="javax.xml.datatype.XMLGregorianCalendar"%>
<%@page import="com.brokering.ShareHistory"%>
<%@page import="java.lang.String"%>
<%@page import="java.util.List"%>
<%@page import="com.brokering.GetShareNewsResponse"%>
<%@page import="com.brokering.ShareNews"%>
<%@page import="java.util.HashMap"%>
<%@page import="java.util.Map"%>
<%@page import="java.util.Date"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>

<%!
    String getCleanDate(XMLGregorianCalendar cal) {
        return String.valueOf(cal.getDay() + "-" + cal.getMonth() + "-" + cal.getYear());
    }
%>

<!DOCTYPE html>
<html>
    <head>
        <link rel="stylesheet" type="text/css" href="style.css"
              <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">

        <script src="libs/jquery-3.2.1.min.js"></script>
        <script src="libs/Chart.min.js"></script>

        <link href="libs/toastr.min.css" rel="stylesheet"/>
        <script src="libs/toastr.min.js"></script>

        <script src="main.js"></script>
        <title>Shares Brokering</title>
    </head>

    <body>

        <%
            java.util.List<java.lang.String> currencies = new java.util.ArrayList<java.lang.String>();
            try {
                com.brokering.ShareBrokeringWS_Service service = new com.brokering.ShareBrokeringWS_Service();
                com.brokering.ShareBrokeringWS port = service.getShareBrokeringWSPort();
                // TODO process result here
                currencies = port.getCurrencies();
            } catch (Exception ex) {
                // TODO handle custom exceptions here
            }
        %>



        <div id="menu">
            <div id="title">
                Shares Brokering
            </div>


            <form name="search" method="get" id="search">
                <input name="searchBox" required></input>

                <select id="currencies" name="currency">
                    <% for (String item : currencies) {
                        String curSplit[] = item.split(" - ");
                        System.out.println(curSplit[0]);
                    %>
                    
                    <option value="<%= curSplit[0] %>"><%out.print(item);%></option>
                    <%}%>
                </select>

                <button onclick="console.log('click')" type="submit">
                    <img class="search-icon" src="res/search.png"></img>
                </button>
            </form>
        </div>






        <%
            java.lang.String reselect = null;
            String[] buySym = request.getParameterValues("buySymbol");
            String[] buyVol = request.getParameterValues("volumeInput");
            String buyResp = null;

            String[] search = request.getParameterValues("searchBox");
            String[] currencyArr = request.getParameterValues("currency");
            String currCode = currencyArr != null ? currencyArr[0] : null;
            
            com.brokering.SearchSharesResponse.Return result = null;

            try {
                com.brokering.ShareBrokeringWS_Service service = new com.brokering.ShareBrokeringWS_Service();
                com.brokering.ShareBrokeringWS port = service.getShareBrokeringWSPort();

                if (buySym != null && buyVol != null) {
                    java.lang.String buyResult = port.buyShares(buySym[0], Integer.parseInt(buyVol[0]), currCode);
                    reselect = buySym[0];
                    System.out.println(buyResult);
                    buyResp = buyResult;
                }

                if (search != null && search[0] != "") {
                    // TODO initialize WS operation arguments here
                    java.lang.String companyName = search[0];
                    java.lang.String companySymbol = "";
                    java.lang.String minShares = "";
                    java.lang.String maxShares = "";
                    java.lang.String currentPrice = "";
                    java.lang.String minPrice = "";
                    java.lang.String maxPrice = "";
                    
                    // TODO process result here
                    result = port.searchShares(companyName, companySymbol, minShares, maxShares, currentPrice, minPrice, maxPrice, currCode);

                    Map<String, List<ShareNews>> companyNews = new HashMap<String, List<ShareNews>>();
                    for (int i = 0; i < result.getShares().size(); i++) {
                        String sym = result.getShares().get(i).getCompanySymobol();

                        List<ShareNews> news = port.getShareNews(sym);
                        companyNews.put(sym, news);
                        System.out.println(news.get(0).getTitle());
                        System.out.println(news.get(1).getTitle());
                        System.out.println(news.get(2).getTitle());
                    }


        %>
        <div id="content">
            <div id="searched" class="column">
                <table>
                    <tr>
                        <th>Company Name</th>
                        <th>Company Symbol</th>
                        <th>Avaliable Shares</th>
                    </tr>
                    <%                        if (result != null) {
                            for (int i = 0; i < result.getShares().size(); i++) { %>
                    <tr onclick="tableClick(this)">
                        <td>
                            <%out.print(result.getShares().get(i).getCompanyName());%>
                        </td>
                        <td>
                            <%out.print(result.getShares().get(i).getCompanySymobol());%>
                        </td>
                        <td>
                            <%out.print(result.getShares().get(i).getAvailableShares());%>
                        </td>
                    </tr>
                    <%
                            }
                        }
                    %>
                </table>
            </div>
            <% for (int i = 0; i < result.getShares().size(); i++) {%>
            <%
                String col1Id = "column_1_" + String.valueOf(i);
                String col2Id = "column_2_" + String.valueOf(i);
            %>


            <div class="column" id="<%= col1Id%>" style="display: none">
                <canvas id="<%= "Chart_" + i%>" width="400" height="400"></canvas>
                    <%
                        List<ShareHistory> shareHist = result.getShares().get(i).getPrice().getHistory();
                    %>
                <script>
                    var ctx = document.getElementById("Chart_" + <%= i%>).getContext('2d');
                    var chart = new Chart(ctx, {
                        type: 'line',
                        data: {
                            labels: ["<%= getCleanDate(shareHist.get(6).getDate())%>",
                                "<%= getCleanDate(shareHist.get(5).getDate())%>",
                                "<%= getCleanDate(shareHist.get(4).getDate())%>",
                                "<%= getCleanDate(shareHist.get(3).getDate())%>",
                                "<%= getCleanDate(shareHist.get(2).getDate())%>",
                                "<%= getCleanDate(shareHist.get(1).getDate())%>",
                                "<%= getCleanDate(shareHist.get(0).getDate())%>"],
                            datasets: [{
                                    label: 'Low',
                                    data: [<%= shareHist.get(6).getLow()%>,
                    <%= shareHist.get(5).getLow()%>,
                    <%= shareHist.get(4).getLow()%>,
                    <%= shareHist.get(3).getLow()%>,
                    <%= shareHist.get(2).getLow()%>,
                    <%= shareHist.get(1).getLow()%>,
                    <%= shareHist.get(0).getLow()%>],
                                    backgroundColor: [
                                        'rgba(128, 0, 128, 0.2)'
                                    ],
                                    borderColor: [
                                        'rgba(255,99,132,1)'
                                    ],
                                    borderWidth: 1,
                                    lineTension: 0
                                },
                                {
                                    label: 'High',
                                    data: [<%= shareHist.get(6).getHigh()%>,
                    <%= shareHist.get(5).getHigh()%>,
                    <%= shareHist.get(4).getHigh()%>,
                    <%= shareHist.get(3).getHigh()%>,
                    <%= shareHist.get(2).getHigh()%>,
                    <%= shareHist.get(1).getHigh()%>,
                    <%= shareHist.get(0).getHigh()%>],
                                    backgroundColor: [
                                        'rgba(128, 0, 128, 0.2)'
                                    ],
                                    borderColor: [
                                        'rgba(255,99,132,1)'
                                    ],
                                    borderWidth: 1,
                                    lineTension: 0
                                },
                                {
                                    label: 'Close',
                                    data: [<%= shareHist.get(6).getClose()%>,
                    <%= shareHist.get(5).getClose()%>,
                    <%= shareHist.get(4).getClose()%>,
                    <%= shareHist.get(3).getClose()%>,
                    <%= shareHist.get(2).getClose()%>,
                    <%= shareHist.get(1).getClose()%>,
                    <%= shareHist.get(0).getClose()%>],
                                    backgroundColor: [
                                        'rgba(128, 0, 128, 0)'
                                    ],
                                    borderColor: [
                                        'rgba(0,0,0,1)'
                                    ],
                                    borderWidth: 1,
                                    lineTension: 0
                                }]
                        },
                        options: {
                            scales: {
                                yAxes: [{
                                        ticks: {
                                            beginAtZero: false
                                        }
                                    }]
                            }
                        }
                    });
                </script> 
                <p>Value: <%= result.getShares().get(i).getPrice().getValue()%> - <%= result.getShares().get(i).getPrice().getCurrency()%></p>
                <form name="buy" method="post" id="<%= "buy_" + i%>">
                    <input name="buySymbol" type="hidden" value="<%= result.getShares().get(i).getCompanySymobol()%>"></input>
                    <input name="volumeInput" type="number" required></input>

                    <button type="submit">Buy</button>
                </form>
            </div>




            <div class="column" id="<%= col2Id%>" style="display: none">
                <div class="tabs">
                    <div class="tab_header active" tab="companyInfo" onclick="switchTab(this)">Company Info</div>
                    <div class="tab_header" tab="companyNews" onclick="switchTab(this)">Company News</div>

                    <span tab="companyInfo">
                        <h1 class="title"><u><a href="<%= "http://" + result.getShares().get(i).getDomain()%>">Company Info</a></u></h1>
                        <h2><%= result.getShares().get(i).getCompanyName()%></h2>
                        <% if (result.getShares().get(i).getCompanyInfo() != null) {%>
                        <h4><%= result.getShares().get(i).getCompanyInfo().getLegalName()%></h4>
                        <span><%= result.getShares().get(i).getCompanyInfo().getCountry()%></span>
                        <p> <%= result.getShares().get(i).getCompanyInfo().getDescription()%></p>
                        <% } %>
                    </span>
                    <span tab="companyNews" style="display: none;">
                        <%
                            List<ShareNews> news = companyNews.get(result.getShares().get(i).getCompanySymobol());
                            for (int j = 0; j < news.size(); j++) {
                                ShareNews newsItem = news.get(j);
                        %>
                        <h4><a href="<%= newsItem.getUrl()%>"><%= newsItem.getTitle()%> </a></h4>
                        <p style="padding-left: 5px;"><%= newsItem.getDescription()%></p>
                        <p style="color: #551a8b"><u><%= newsItem.getAuthor()%>  -  <%= newsItem.getSource()%></u></p>
                        <p></p>
                        <%
                            }
                        %>
                    </span>
                </div>

            </div>
            <%
                }
                if (reselect != null) {
            %>
            <script>
                //reselect the previous share
                var previous = $('#searched tr > td:contains("<%= reselect%>")').parent();
                previous.trigger('click');
            </script>
            <%
                    reselect = null;
                }
                if (buyResp != null) {
            %>
            <script>
                toastr.options = {"positionClass": "toast-top-full-width", "closeButton": true};
                toastr.info('<%= buyResp%>');
            </script>
            <%
                    buyResp = null;
                }

            %>

        </div>
        <%                }
            } catch (Exception ex) {
                // TODO handle custom exceptions here
                System.out.println(ex);
            }

        %>

    </body>

</html>
