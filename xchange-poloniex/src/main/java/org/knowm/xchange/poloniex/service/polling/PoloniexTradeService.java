package org.knowm.xchange.poloniex.service.polling;

/**
 * @author Zach Holmes
 */

import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;

import org.knowm.xchange.Exchange;
import org.knowm.xchange.currency.CurrencyPair;
import org.knowm.xchange.dto.Order;
import org.knowm.xchange.dto.Order.OrderType;
import org.knowm.xchange.dto.marketdata.Trades.TradeSortType;
import org.knowm.xchange.dto.trade.*;
import org.knowm.xchange.exceptions.ExchangeException;
import org.knowm.xchange.exceptions.NotAvailableFromExchangeException;
import org.knowm.xchange.exceptions.NotYetImplementedForExchangeException;
import org.knowm.xchange.poloniex.PoloniexAdapters;
import org.knowm.xchange.poloniex.PoloniexUtils;
import org.knowm.xchange.poloniex.dto.trade.PoloniexLimitOrder;
import org.knowm.xchange.poloniex.dto.trade.PoloniexOpenOrder;
import org.knowm.xchange.poloniex.dto.trade.PoloniexTradeResponse;
import org.knowm.xchange.poloniex.dto.trade.PoloniexUserTrade;
import org.knowm.xchange.service.polling.trade.PollingTradeService;
import org.knowm.xchange.service.polling.trade.params.TradeHistoryParamCurrencyPair;
import org.knowm.xchange.service.polling.trade.params.TradeHistoryParams;
import org.knowm.xchange.service.polling.trade.params.TradeHistoryParamsAll;
import org.knowm.xchange.service.polling.trade.params.TradeHistoryParamsTimeSpan;
import org.knowm.xchange.service.polling.trade.params.orders.OpenOrdersParams;
import org.knowm.xchange.utils.DateUtils;

public class PoloniexTradeService extends PoloniexTradeServiceRaw implements PollingTradeService {

  public PoloniexTradeService(Exchange exchange) {

    super(exchange);
  }

  @Override
  public OpenOrders getOpenOrders() throws IOException {
    return getOpenOrders(createOpenOrdersParams());
  }

  @Override
  public OpenOrders getOpenOrders(OpenOrdersParams params) throws ExchangeException, NotAvailableFromExchangeException, NotYetImplementedForExchangeException, IOException {
    HashMap<String, PoloniexOpenOrder[]> poloniexOpenOrders = returnOpenOrders();
    return PoloniexAdapters.adaptPoloniexOpenOrders(poloniexOpenOrders);
  }

  @Override
  public String placeMarketOrder(MarketOrder marketOrder) throws IOException {

    throw new NotAvailableFromExchangeException();
  }

  @Override
  public String placeLimitOrder(LimitOrder limitOrder) throws IOException {

    PoloniexTradeResponse response;
    if (limitOrder.getType() == OrderType.BID) {
      response = buy(limitOrder);
    } else {
      response = sell(limitOrder);
    }

    // The return value contains details of any trades that have been immediately executed as a result  
    // of this order. Make these available to the application if it has provided a PoloniexLimitOrder. 
    if (limitOrder instanceof PoloniexLimitOrder) {
      PoloniexLimitOrder raw = (PoloniexLimitOrder) limitOrder;
      raw.setResponse(response);
    }

    return response.getOrderNumber().toString();
  }

  @Override
  public boolean cancelOrder(String orderId) throws IOException {

    return cancel(orderId);
  }

  /**
   * @param params Can optionally implement {@link TradeHistoryParamCurrencyPair} and {@link TradeHistoryParamsTimeSpan}. All other TradeHistoryParams
   *        types will be ignored.
   */
  @Override
  public UserTrades getTradeHistory(TradeHistoryParams params) throws IOException {

    CurrencyPair currencyPair = null;
    Date startTime = null;
    Date endTime = null;

    if (params instanceof TradeHistoryParamCurrencyPair) {
      currencyPair = ((TradeHistoryParamCurrencyPair) params).getCurrencyPair();
    }
    if (params instanceof TradeHistoryParamsTimeSpan) {
      startTime = ((TradeHistoryParamsTimeSpan) params).getStartTime();
      endTime = ((TradeHistoryParamsTimeSpan) params).getEndTime();
    }
    return getTradeHistory(currencyPair, DateUtils.toUnixTimeNullSafe(startTime), DateUtils.toUnixTimeNullSafe(endTime));
  }

  public BigDecimal getMakerFee() throws IOException {
    String value = getFeeInfo().get("makerFee");
    return new BigDecimal(value);
  }

  public BigDecimal getTakerFee() throws IOException {
    String value = getFeeInfo().get("takerFee");
    return new BigDecimal(value);
  }

  private UserTrades getTradeHistory(CurrencyPair currencyPair, final Long startTime, final Long endTime) throws IOException {

    List<UserTrade> trades = new ArrayList<UserTrade>();
    if (currencyPair == null) {
      HashMap<String, PoloniexUserTrade[]> poloniexUserTrades = returnTradeHistory(startTime, endTime);
      if (poloniexUserTrades != null) {
        for (Map.Entry<String, PoloniexUserTrade[]> mapEntry : poloniexUserTrades.entrySet()) {
          currencyPair = PoloniexUtils.toCurrencyPair(mapEntry.getKey());
          for (PoloniexUserTrade poloniexUserTrade : mapEntry.getValue()) {
            trades.add(PoloniexAdapters.adaptPoloniexUserTrade(poloniexUserTrade, currencyPair));
          }
        }
      }
    } else {
      PoloniexUserTrade[] poloniexUserTrades = returnTradeHistory(currencyPair, startTime, endTime);
      if (poloniexUserTrades != null) {
        for (PoloniexUserTrade poloniexUserTrade : poloniexUserTrades) {
          trades.add(PoloniexAdapters.adaptPoloniexUserTrade(poloniexUserTrade, currencyPair));
        }
      }
    }

    return new UserTrades(trades, TradeSortType.SortByTimestamp);
  }

  /**
   * Create {@link TradeHistoryParams} that supports {@link TradeHistoryParamsTimeSpan} and {@link TradeHistoryParamCurrencyPair}.
   */

  @Override
  public TradeHistoryParams createTradeHistoryParams() {

    return new PoloniexTradeHistoryParams();
  }

  @Override
  public OpenOrdersParams createOpenOrdersParams() {
    return null;
  }

  @Override
  public Collection<Order> getOrder(String... orderIds)
      throws ExchangeException, NotAvailableFromExchangeException, NotYetImplementedForExchangeException, IOException {
    throw new NotYetImplementedForExchangeException();
  }

  public static class PoloniexTradeHistoryParams implements TradeHistoryParamCurrencyPair, TradeHistoryParamsTimeSpan {

    private final TradeHistoryParamsAll all = new TradeHistoryParamsAll();

    @Override
    public void setCurrencyPair(CurrencyPair value) {

      all.setCurrencyPair(value);
    }

    @Override
    public CurrencyPair getCurrencyPair() {

      return all.getCurrencyPair();
    }

    @Override
    public void setStartTime(Date value) {

      all.setStartTime(value);
    }

    @Override
    public Date getStartTime() {

      return all.getStartTime();
    }

    @Override
    public void setEndTime(Date value) {

      all.setEndTime(value);
    }

    @Override
    public Date getEndTime() {

      return all.getEndTime();
    }
  }

}
