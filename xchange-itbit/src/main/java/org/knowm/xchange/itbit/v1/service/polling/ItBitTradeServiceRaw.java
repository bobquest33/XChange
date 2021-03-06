package org.knowm.xchange.itbit.v1.service.polling;

import java.io.IOException;
import java.util.Date;

import org.knowm.xchange.Exchange;
import org.knowm.xchange.currency.CurrencyPair;
import org.knowm.xchange.dto.Order.OrderType;
import org.knowm.xchange.dto.trade.LimitOrder;
import org.knowm.xchange.itbit.v1.ItBitAdapters;
import org.knowm.xchange.itbit.v1.dto.ItBitException;
import org.knowm.xchange.itbit.v1.dto.trade.ItBitOrder;
import org.knowm.xchange.itbit.v1.dto.trade.ItBitPlaceOrderRequest;
import org.knowm.xchange.itbit.v1.dto.trade.ItBitTradeHistory;

public class ItBitTradeServiceRaw extends ItBitBasePollingService {

  /**
   * Constructor
   *
   * @param exchange
   */
  public ItBitTradeServiceRaw(Exchange exchange) {
    super(exchange);
  }

  public ItBitOrder[] getItBitOpenOrders(CurrencyPair currencyPair) throws IOException {

    ItBitOrder[] orders = itBitAuthenticated.getOrders(signatureCreator, new Date().getTime(), exchange.getNonceFactory(),
        ItBitAdapters.adaptCurrency(currencyPair.base).getCurrencyCode() + ItBitAdapters.adaptCurrency(currencyPair.counter).getCurrencyCode(),
        "1", "1000",
        "open",
        walletId);

    return orders;
  }

  /**
   * Retrieves the set of orders with the given status.
   *
   * @param status
   * @return
   * @throws IOException
   */
  public ItBitOrder[] getItBitOrders(String status) throws IOException {

    ItBitOrder[] orders = itBitAuthenticated.getOrders(signatureCreator, new Date().getTime(), exchange.getNonceFactory(), "XBTUSD", "1", "1000",
        status, walletId);

    return orders;
  }

  public ItBitOrder getItBitOrder(String orderId) throws IOException {

    ItBitOrder order = itBitAuthenticated.getOrder(signatureCreator, new Date().getTime(), exchange.getNonceFactory(), walletId, orderId);

    return order;
  }

  public ItBitOrder placeItBitLimitOrder(LimitOrder limitOrder) throws IOException {

    String side = limitOrder.getType().equals(OrderType.BID) ? "buy" : "sell";
    String baseCurrency = ItBitAdapters.adaptCurrency(limitOrder.getCurrencyPair().base).getCurrencyCode();
    String amount = ItBitAdapters.formatCryptoAmount(limitOrder.getTradableAmount());
    String price = ItBitAdapters.formatFiatAmount(limitOrder.getLimitPrice());

    ItBitOrder postOrder = itBitAuthenticated.postOrder(signatureCreator, new Date().getTime(), exchange.getNonceFactory(), walletId,
        new ItBitPlaceOrderRequest(side, "limit", baseCurrency, amount, price,
            baseCurrency + limitOrder.getCurrencyPair().counter.getCurrencyCode()));

    return postOrder;
  }

  public void cancelItBitOrder(String orderId) throws IOException {

    itBitAuthenticated.cancelOrder(signatureCreator, new Date().getTime(), exchange.getNonceFactory(), walletId, orderId);
  }

  public ItBitOrder[] getItBitOrderHistory(String currency, String pageNum, String pageLen) throws IOException {

    ItBitOrder[] orders = itBitAuthenticated.getOrders(signatureCreator, new Date().getTime(), exchange.getNonceFactory(), currency, pageNum, pageLen,
        "filled", walletId);
    return orders;
  }

  public ItBitTradeHistory getUserTradeHistory(String lastExecutionId, Integer page, Integer perPage, Date rangeStart, Date rangeEnd)
      throws IOException, ItBitException {
    return itBitAuthenticated.getUserTradeHistory(signatureCreator, System.currentTimeMillis(), exchange.getNonceFactory(), walletId, lastExecutionId,
        page, perPage, rangeStart, rangeEnd);
  }
}
