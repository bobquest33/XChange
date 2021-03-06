package org.knowm.xchange.kraken.service.polling;

import java.io.IOException;
import java.util.Collection;

import org.knowm.xchange.Exchange;
import org.knowm.xchange.dto.Order;
import org.knowm.xchange.dto.trade.LimitOrder;
import org.knowm.xchange.dto.trade.MarketOrder;
import org.knowm.xchange.dto.trade.OpenOrders;
import org.knowm.xchange.dto.trade.UserTrades;
import org.knowm.xchange.exceptions.ExchangeException;
import org.knowm.xchange.exceptions.NotAvailableFromExchangeException;
import org.knowm.xchange.exceptions.NotYetImplementedForExchangeException;
import org.knowm.xchange.kraken.KrakenAdapters;
import org.knowm.xchange.service.polling.trade.PollingTradeService;
import org.knowm.xchange.service.polling.trade.params.DefaultTradeHistoryParamsTimeSpan;
import org.knowm.xchange.service.polling.trade.params.TradeHistoryParamOffset;
import org.knowm.xchange.service.polling.trade.params.TradeHistoryParams;
import org.knowm.xchange.service.polling.trade.params.TradeHistoryParamsTimeSpan;
import org.knowm.xchange.service.polling.trade.params.orders.OpenOrdersParams;
import org.knowm.xchange.utils.DateUtils;

public class KrakenTradeService extends KrakenTradeServiceRaw implements PollingTradeService {

  /**
   * Constructor
   *
   * @param exchange
   */
  public KrakenTradeService(Exchange exchange) {

    super(exchange);
  }

  @Override
  public OpenOrders getOpenOrders() throws IOException {
    return getOpenOrders(createOpenOrdersParams());
  }

  @Override
  public OpenOrders getOpenOrders(OpenOrdersParams params) throws ExchangeException, NotAvailableFromExchangeException, NotYetImplementedForExchangeException, IOException {
    return KrakenAdapters.adaptOpenOrders(super.getKrakenOpenOrders());
  }

  @Override
  public String placeMarketOrder(MarketOrder marketOrder) throws IOException {

    return KrakenAdapters.adaptOrderId(super.placeKrakenMarketOrder(marketOrder));
  }

  @Override
  public String placeLimitOrder(LimitOrder limitOrder) throws IOException {

    return KrakenAdapters.adaptOrderId(super.placeKrakenLimitOrder(limitOrder));
  }

  @Override
  public boolean cancelOrder(String orderId) throws IOException {

    return super.cancelKrakenOrder(orderId).getCount() > 0;
  }

  /**
   * @param params Can optionally implement {@link TradeHistoryParamOffset} and {@link TradeHistoryParamsTimeSpan}. All other TradeHistoryParams types
   *        will be ignored.
   */
  @Override
  public UserTrades getTradeHistory(TradeHistoryParams params) throws ExchangeException, IOException {

    final Long startTime;
    final Long endTime;
    if (params instanceof TradeHistoryParamsTimeSpan) {
      TradeHistoryParamsTimeSpan timeSpan = (TradeHistoryParamsTimeSpan) params;
      startTime = DateUtils.toUnixTimeNullSafe(timeSpan.getStartTime());
      endTime = DateUtils.toUnixTimeNullSafe(timeSpan.getEndTime());
    } else {
      startTime = null;
      endTime = null;
    }

    final Long offset;
    if (params instanceof TradeHistoryParamOffset) {
      offset = ((TradeHistoryParamOffset) params).getOffset();
    } else {
      offset = null;
    }

    return KrakenAdapters.adaptTradesHistory(getKrakenTradeHistory(null, false, startTime, endTime, offset));
  }

  @Override
  public org.knowm.xchange.service.polling.trade.params.TradeHistoryParams createTradeHistoryParams() {

    return new KrakenTradeHistoryParams();
  }

  @Override
  public OpenOrdersParams createOpenOrdersParams() {
    return null;
  }

  public static class KrakenTradeHistoryParams extends DefaultTradeHistoryParamsTimeSpan implements TradeHistoryParamOffset {

    private Long offset;

    @Override
    public void setOffset(Long offset) {
      this.offset = offset;
    }

    @Override
    public Long getOffset() {
      return offset;
    }
  }

  @Override
  public Collection<Order> getOrder(String... orderIds)
      throws ExchangeException, NotAvailableFromExchangeException, NotYetImplementedForExchangeException, IOException {
    throw new NotYetImplementedForExchangeException();
  }

}
