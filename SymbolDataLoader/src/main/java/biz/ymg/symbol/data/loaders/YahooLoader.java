package biz.ymg.symbol.data.loaders;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.Block;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.ReplaceOptions;
import com.mongodb.client.result.UpdateResult;

import biz.ymg.symbol.data.HistoricalPriceStore;
import biz.ymg.symbol.data.SymbolList;

public class YahooLoader {

	private static String mongoSvr = "historicalpricestore0-h0pai.mongodb.net";
	private static String rwPass = "Ki4,8BNo6SbA1CbTvuY50.RW";
	private static String rwUser = "HistoricalPriceStore0_RW";
	private static String mongoCxn = "mongodb+srv://" + rwUser + ":" + rwPass + "@" + mongoSvr
			+ "/test?retryWrites=true";

	public static String jsonFix(String jsonInString) {
		String fromQuote = "'";
		String toQuote = "\"";
		return new String(jsonInString).replace(fromQuote, toQuote);
	}

	public static void main(String[] args) throws Exception {
		YahooLoader loader = new YahooLoader();
		for (String priceInfo : loader.getHistoricalPriceStore("AAPL")) {
			print(priceInfo);
		}
		loader.loadData();
		loader.decode("日本語");
		loader.toAscii("日本語");
		loader.decode("1\u002F20");
		loader.toAscii("1\u002F20");
		// some test code here ...
		loader.addAndFetchData();
		print(loader.datetimeFromLong(1530624600));
		print(loader.datetimeFromLong(1531771201));
	}

	public static void print(String message) {
		System.out.println("INFO: " + message);
	}

	public static final Document toDocument(String pennyStock, String jsonInString)
			throws JsonParseException, JsonMappingException, IOException {
		ObjectMapper mapper = new ObjectMapper();
		// print(pennyStock + " >>> " + jsonInString);
		HistoricalPriceStore obj = mapper.readValue(jsonInString, HistoricalPriceStore.class);
		return new Document("date", obj.getDate()).append("open", obj.getOpen()).append("high", obj.getHigh())
				.append("low", obj.getLow()).append("close", obj.getClose()).append("volume", obj.getVolume())
				.append("adjclose", obj.getAdjclose()).append("numerator", obj.getNumerator())
				.append("denominator", obj.getDenominator()).append("splitRatio", obj.getSplitRatio())
				.append("type", obj.getType()).append("data", obj.getData()).append("amount", obj.getAmount());
	}

	public Block<Document> printBlock = new Block<Document>() {
		@Override
		public void apply(final Document document) {
			print(document.toJson());
		}
	};

	public String decode(String incoming) throws UnsupportedEncodingException {
		// print("Original input string from client: " + incoming);

		String encoded = URLEncoder.encode(incoming, "UTF-8");
		// print("URL-encoded by client with UTF-8: " + encoded);

		// String incorrectDecoded = URLDecoder.decode(encoded, "ISO-8859-1");
		// print("Then URL-decoded by server with ISO-8859-1: " + incorrectDecoded);

		String correctDecoded = URLDecoder.decode(encoded, "UTF-8");
		// print("Server should URL-decode with UTF-8: " + correctDecoded);

		return correctDecoded;
	}

	public YahooLoader() {
	}

	public void addAndFetchData() throws Exception {
		MongoClient mongoClient = MongoClients.create(mongoCxn);
		MongoDatabase database = mongoClient.getDatabase("test");
		String collectionName = "posts";
		MongoCollection<Document> collection = database.getCollection(collectionName);
		Document post = new Document("author", "Mike").append("text", "My first Java blog post!").append("count", 1)
				.append("date", new Date()).append("tags", Arrays.asList("mongodb", "java", "javaMongo"))
				.append("info", new Document("x", 203).append("y", 102));
		collection.insertOne(post);
		ObjectId post_id = (ObjectId) post.get("_id");
		print(String.valueOf(post_id));
		collection.find(Filters.eq("text", "My first Java blog post!")).forEach(printBlock);
		mongoClient.close();
	}

	public String datetimeFromLong(long longDateString) throws Exception {
		return datetimeFromLong(String.valueOf(longDateString));
	}

	public String datetimeFromLong(String longDateString) throws Exception {
		long longDate = Long.valueOf(longDateString);
		Date date = new Date(longDate * 1000L);
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		return format.format(date);
	}

	public List<String> getHistoricalPriceStore(String tickerSymbol) throws Exception {
		String url = makeUrl(tickerSymbol);
		String chunks = "";
		print(url);
		URL symbUrl = new URL(url);
		BufferedReader in = new BufferedReader(new InputStreamReader(symbUrl.openStream(), StandardCharsets.UTF_8));

		String inputLine;
		while ((inputLine = in.readLine()) != null) {
			chunks += toAscii(inputLine);
		}
		in.close();

		String[] parts = chunks.split("HistoricalPriceStore");
		String[] pricesArray = parts[1].split("[]]");
		String[] actualPricesArray = pricesArray[0].split("[\\[]");
		String[] dataOutTmp = actualPricesArray[1].split("[}]");
		List<String> dataOut = new ArrayList<String>();
		for (String actualPrice : dataOutTmp) {
			if (actualPrice.startsWith(",")) {
				actualPrice = actualPrice.replaceFirst("[,]", "");
			}
			actualPrice = actualPrice + "}";
			dataOut.add(actualPrice);
		}
		return dataOut;
	}

	public void loadData() throws Exception {
		print("Starting the load process ...");

		String[] allSymbols = SymbolList.ALL_SYMBOLS;

		MongoClient mongoClient = MongoClients.create(mongoCxn);
		MongoDatabase database = mongoClient.getDatabase("test");

		for (String currSymbol : allSymbols) {
			String message = "";
			String resultMessages = "";
			List<String> prices = getHistoricalPriceStore(currSymbol);
			MongoCollection<Document> collection = database.getCollection(currSymbol);
			long previousCount = collection.countDocuments();
			long incomingCount = prices.size();
			for (String price : prices) {
				String priceString = jsonFix(price);
				Document priceDetails = toDocument(currSymbol, priceString);
				long dateKey = priceDetails.getLong("date");
				Bson searchArg = Filters.eq("date", dateKey);
				int matchIndex = 0;
				String matchMessages = "";
				FindIterable<Document> matches = collection.find(searchArg);
				for (Document match : matches) {
					matchIndex += 1;
					matchMessages += String.valueOf(matchIndex) + "=>" + String.valueOf(match) + ";";
				}
				if (matchIndex == 0) {
					message = String.valueOf(currSymbol) + " No matches for ["
							+ String.valueOf(datetimeFromLong(dateKey)) + "] " + String.valueOf(searchArg);
					print(message);
				}
				if (matchIndex > 1) {
					message = String.valueOf(currSymbol) + " Multiple matches for ["
							+ String.valueOf(datetimeFromLong(dateKey)) + "] " + String.valueOf(searchArg) + " => "
							+ String.valueOf(matchMessages);
					print(message);
				}
				UpdateResult result = collection.replaceOne(searchArg, priceDetails, new ReplaceOptions().upsert(true));
				if (matchIndex != 1) {
					resultMessages += String.valueOf(currSymbol) + " DATE: " + String.valueOf(datetimeFromLong(dateKey))
							+ " [" + String.valueOf(dateKey) + "] result=" + String.valueOf(result.toString()) + "\n";
				}
			}
			long currentCount = collection.countDocuments();
			if (incomingCount != currentCount) {
				message = String.valueOf(currSymbol) + " STATS: incoming_count=" + String.valueOf(incomingCount)
						+ "; previous_count=" + String.valueOf(previousCount) + "; currentCount="
						+ String.valueOf(currentCount);
				print(message);
				if (resultMessages.length() > 0) {
					print(resultMessages);
				}
			}
		}

		mongoClient.close();

		print("All done!");
	}

	public String makeUrl(String tickerSymbol) throws Exception {
		String symbKey = "@@SYM@@";
		String baseUrl = "https://finance.yahoo.com/quote/" + symbKey + "/history?p=" + symbKey;
		return baseUrl.replace(symbKey, tickerSymbol);
	}

	public String toAscii(String incoming) throws UnsupportedEncodingException {
		// print("incoming String >>> " + incoming);
		byte[] bytes = incoming.getBytes(StandardCharsets.ISO_8859_1); // Charset to encode into
		// print("incoming byte[] >>> " + incoming);
		String outgoing = new String(bytes, StandardCharsets.UTF_8); // Charset with which bytes were encoded
		outgoing = outgoing.replace("\\u002F", "/"); // Last ditch effort to remove known unwanted encoding
		// print("outgoing String >>> " + outgoing);
		return outgoing;
	}
}
