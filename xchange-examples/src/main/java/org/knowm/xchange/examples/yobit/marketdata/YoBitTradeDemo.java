package org.knowm.xchange.examples.yobit.marketdata;

import java.io.IOException;

import org.knowm.xchange.Exchange;
import org.knowm.xchange.ExchangeFactory;
import org.knowm.xchange.currency.CurrencyPair;
import org.knowm.xchange.dto.marketdata.Trade;
import org.knowm.xchange.dto.marketdata.Trades;
import org.knowm.xchange.service.polling.marketdata.PollingMarketDataService;
import org.knowm.xchange.yobit.YoBitExchange;

/**
 * Demonstrate requesting Trade from YoBit..
 */
public class YoBitTradeDemo {

	public static void main(String[] args) throws IOException {

		Exchange yoBitExchange = ExchangeFactory.INSTANCE.createExchange(YoBitExchange.class.getName());

		// Interested in the public market data feed (no authentication)
		PollingMarketDataService marketDataService = yoBitExchange.getPollingMarketDataService();

		System.out.println("fetching data...");
		
		Trades trades = marketDataService.getTrades(CurrencyPair.BTC_USD);

		System.out.println("received data.");

		for (Trade trade : trades.getTrades()) {
			System.out.println(trade.getType() + " " + trade.getCurrencyPair() + " Price: "
					+ trade.getPrice() + " Amount: " + trade.getTradableAmount());
		}
	}

}
