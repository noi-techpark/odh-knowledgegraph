package semanticdw;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.io.IOUtils;
import org.gnome.gdk.Event;
import org.gnome.gdk.Pixbuf;
import org.gnome.gdk.RGBA;
import org.gnome.gtk.Builder;
import org.gnome.gtk.Button;
import org.gnome.gtk.Button.Clicked;
import org.gnome.gtk.CellRendererText;
import org.gnome.gtk.DataColumn;
import org.gnome.gtk.DataColumnInteger;
import org.gnome.gtk.DataColumnString;
import org.gnome.gtk.Dialog;
import org.gnome.gtk.FileChooserAction;
import org.gnome.gtk.FileChooserDialog;
import org.gnome.gtk.FileFilter;
import org.gnome.gtk.Gtk;
import org.gnome.gtk.ListStore;
import org.gnome.gtk.ResponseType;
import org.gnome.gtk.ScrolledWindow;
import org.gnome.gtk.SortType;
import org.gnome.gtk.StateFlags;
import org.gnome.gtk.TextBuffer;
import org.gnome.gtk.TextIter;
import org.gnome.gtk.TextTag;
import org.gnome.gtk.TextTagTable;
import org.gnome.gtk.TextView;
import org.gnome.gtk.ToggleToolButton;
import org.gnome.gtk.ToolButton;
import org.gnome.gtk.TreeIter;
import org.gnome.gtk.TreePath;
import org.gnome.gtk.TreeView;
import org.gnome.gtk.TreeViewColumn;
import org.gnome.gtk.Widget;
import org.gnome.gtk.Window;
import org.gnome.gtk.WrapMode;
import org.gnome.pango.FontDescription;
import org.gnome.sourceview.SourceBuffer;
import org.gnome.sourceview.SourceView;

public class Main {

	private static final String GLADEFILE = "gui.glade";

	private static ListStore listStoreTable;
	private static ListStore listStoreQueryNames;
	private static DataColumn[] dataColumns;

	private static TreeView treeviewTable;
	private static ToggleToolButton buttonIRI;

	private static TextTagTable textTagTable;
	private static TextTag textTagRed;
	private static TextTag textTagSmall;
	private static TextTag textTagHeader;

	private static QFileParser tourismQueries;

	private static SourceView sourceView;

	private static DBPedia dbpedia;

	private static String currentFolder;

	public static void fetchImageFromURL(String url, String destinationFile){
		try {
			URL obj = new URL(url);
		    HttpURLConnection conn = (HttpURLConnection) obj.openConnection();

			boolean redirect = false;
			// normally, 3xx is redirect
			int status = conn.getResponseCode();
			if (status != HttpURLConnection.HTTP_OK) {
				if (status == HttpURLConnection.HTTP_MOVED_TEMP
					|| status == HttpURLConnection.HTTP_MOVED_PERM
					|| status == HttpURLConnection.HTTP_SEE_OTHER)
					redirect = true;
			}
			if (redirect) {
				// get redirect url from "location" header field
				String newUrl = conn.getHeaderField("Location");
				// get the cookie if need, for login
				String cookies = conn.getHeaderField("Set-Cookie");
				// open the new connnection again
				obj = new URL(newUrl);
				conn = (HttpURLConnection) obj.openConnection();
				conn.setRequestProperty("Cookie", cookies);
			}

			// optional default is GET
		    conn.setRequestMethod("GET");

		    //add request header
		    conn.setRequestProperty("User-Agent", "Mozilla/5.0");

		    int responseCode = conn.getResponseCode();
		    System.out.println("\nSending 'GET' request to URL : " + url);
		    System.out.println("Response Code : " + responseCode);

		    InputStream in = conn.getInputStream();
		    OutputStream out = new FileOutputStream(destinationFile);
		    try {
		    	byte[] bytes = new byte[2048];
		    	int length;

		    	while ((length = in.read(bytes)) != -1) {
		    		out.write(bytes, 0, length);
		    	}
		    } finally {
		    	in.close();
		    	out.close();
		    }
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static void showError(final String text, TextBuffer b) {
		b.setText("ERROR:\n" + text);
		b.applyTag(textTagRed, b.getIterStart(), b.getIterEnd());
	}

	public static void main(String[] args) {

		/*
		 * Build the GUI from a GTK+ Glade file
		 */
		Gtk.init(args);
		Builder gtkBuilder = new Builder();
		try {
			InputStream n = Main.class.getResourceAsStream(GLADEFILE);
			String glade = IOUtils.toString(n, "UTF-8");
			gtkBuilder.addFromString(glade);
		} catch (Exception e) {
			System.err.format("ERROR: Cannot load GUI file (%s).", GLADEFILE);
			e.printStackTrace(System.err);
		}

		final Window window = (Window) gtkBuilder.getObject("window1");
		final ToolButton buttonExec = (ToolButton) gtkBuilder.getObject("executeButton");
		final ToolButton buttonOpen = (ToolButton) gtkBuilder.getObject("open");
		buttonIRI = (ToggleToolButton) gtkBuilder.getObject("fullIRI");

		/*
		 * Retrieve and configure all text output related widgets
		 */
		final TextView textViewSQL = (TextView) gtkBuilder.getObject("textview2");
		final TextView textViewInfo = (TextView) gtkBuilder.getObject("textview3");
		final TextBuffer textBufferSQL = (TextBuffer) gtkBuilder.getObject("textBufferSQL");
		final TextBuffer textBufferInfo = (TextBuffer) gtkBuilder.getObject("textBufferInfo");
		final Dialog dialog = (Dialog) gtkBuilder.getObject("dialog1");
		final Button buttonDialog = (Button) gtkBuilder.getObject("button1");
		final TextBuffer textBufferDialog = (TextBuffer) gtkBuilder.getObject("textBufferDialog");

		textTagTable = (TextTagTable) gtkBuilder.getObject("texttagtable");
		textTagRed = new TextTag(textTagTable, "red");
		textTagRed.setForeground("red");
		textTagSmall = new TextTag(textTagTable, "small");
		textTagSmall.setSize(7);
		textTagHeader = new TextTag(textTagTable, "header");
		textTagHeader.setFontDescription(new FontDescription("Helvetica, 16"));
		textTagHeader.setPaddingBelowParagraph(20);

		textViewSQL.setBuffer(textBufferSQL);
		textViewSQL.overrideFont(new FontDescription("Monospace, 8"));
		textViewSQL.overrideBackground(StateFlags.NORMAL, new RGBA(211,215,207,0.9));
		textViewInfo.setBuffer(textBufferInfo);
		textViewInfo.setWrapMode(WrapMode.WORD);

		final ScrolledWindow sw = (ScrolledWindow) gtkBuilder.getObject("scrolledwindow1");
		final SourceBuffer textBufferCode = new SourceBuffer();
		sourceView = new SourceView(textBufferCode);
		sourceView.setAutoIndent(true);
		sourceView.setHighlightCurrentLine(true);
		sourceView.setInsertSpacesInsteadOfTabs(true);
		sourceView.setTabWidth(4);
		sourceView.setShowLineNumbers(true);
		sourceView.overrideFont(new FontDescription("Monospace, 8"));
		sourceView.setWrapMode(WrapMode.WORD_CHAR);
		sw.add(sourceView);

		/*
		 * Retrieve and configure all list (or table) widgets
		 */
		final TreeView treeViewQueryNames = (TreeView) gtkBuilder.getObject("treeview1");
		treeviewTable = (TreeView) gtkBuilder.getObject("treeview2");
		final DataColumnString sectionName;

		listStoreQueryNames = new ListStore(new DataColumn[] {
				sectionName = new DataColumnString(),
		});

		TreeViewColumn vertical = treeViewQueryNames.appendColumn();
		vertical.setTitle("Query");
		CellRendererText renderer = new CellRendererText(vertical);
		renderer.setMarkup(sectionName);
		treeViewQueryNames.setModel(listStoreQueryNames);

		window.showAll();

		try {
			dbpedia = new DBPedia();
		} catch (IOException e) {
			showError(e.toString(), textBufferInfo);
		}

		buttonExec.connect(new ToolButton.Clicked() {
			@Override
			public void onClicked(ToolButton arg0) {
				try {
					QuestOWLExample example = new QuestOWLExample();
					String sparqlQuery = textBufferCode.getText();
					example.runQuery(sparqlQuery);

					textBufferSQL.setText(example.getSqlQuery());

					String resulttext = example.getResults().toString();
					if (! buttonIRI.getActive()) {
						resulttext = QFileParser.replaceLongIRIs(resulttext, sparqlQuery);
					}
					textViewInfo.setWrapMode(WrapMode.NONE);
					textBufferInfo.setText("SPARQL QUERY EXECUTION SUCCESSFUL");

					List<List<String>> result = example.getResults();
					if (result.size() == 0) {
						emptyTable();
						throw new Exception("Query did not return any result");
					}

					Aggregation count = new Aggregation(result);
					Map<List<String>, Integer> groupings = count.getResult();
					for (Entry<List<String>, Integer> e : groupings.entrySet()) {
						System.out.println("group:" + e.getKey() + " -> " + e.getValue());
					}

					List<String> columnNames = example.getSignature();
					columnNames.add("count");
					createTable(columnNames, groupings, sparqlQuery);

				} catch (Exception e) {
					e.printStackTrace(System.err);
					showError(e.getMessage(), textBufferInfo);
				}
			}
		});

		treeviewTable.connect(new TreeView.RowActivated() {

			@Override
			public void onRowActivated(TreeView source, TreePath path, TreeViewColumn column) {
                final TreeIter row;
                final String value;
                int pos = Integer.parseInt(column.getTitle().substring(0, column.getTitle().indexOf(':')).trim());
                row = listStoreTable.getIter(path);
                value = listStoreTable.getValue(row, (DataColumnString) dataColumns[pos]);

                try {
                	dbpedia.fetchDBPedia(value);

					Map<String, String> result = dbpedia.getResult();

					textBufferDialog.setText("");
					TextIter textPos = textBufferDialog.getIterEnd();

					File imageFile = new File("res/imagecache/" + dbpedia.getTown() + ".jpg");
					try {
						if (! imageFile.exists()) {
							fetchImageFromURL(dbpedia.getThumbnail(), imageFile.toString());
						}
						textBufferDialog.insert(textPos, new Pixbuf(imageFile.toString()));
					} catch (Exception e) {
						showError(e.getMessage(), textBufferInfo);
					}
					textBufferDialog.insert(textPos, "\n");
					textBufferDialog.insert(textPos, dbpedia.getTown(), textTagHeader);
					textBufferDialog.insert(textPos, "\n");
					dialog.setTitle(dbpedia.getTown() + ": Information from DBPEDIA.ORG");

					ArrayList<String> keys = new ArrayList<String>(result.keySet());
					Collections.sort(keys);

					for(String key : keys) {
						String v = result.get(key);
						textPos = textBufferDialog.getIterEnd();
						System.out.println(key + ": " + v);
						String x = key.replaceFirst(".*/([^/?]+).*", "$1");
						x = x.replaceAll("([A-Z][A-Z]*)", " $1").toUpperCase();
						textBufferDialog.insert(textPos, x + ":  ", textTagSmall);
						textBufferDialog.insert(textPos, v + "\n");
					}

					if(textBufferDialog.getText().length() == 0) {
						throw new Exception("No dbpedia entry found for " + dbpedia.getTown());
					}

					dialog.setTransientFor(window);
					dialog.show();
					dialog.run();
					dialog.hide();

                } catch (Exception e) {
					e.printStackTrace(System.err);
					showError(e.getMessage(), textBufferInfo);
                }
			}


		});

		buttonOpen.connect(new ToolButton.Clicked() {

			@Override
			public void onClicked(ToolButton arg0) {
				FileChooserDialog dialogOpen = new FileChooserDialog("Open SPARQL query file", window, FileChooserAction.OPEN);
				FileFilter filter = new FileFilter();
				filter.addPattern("*.q");
				dialogOpen.setFilter(filter);
				if (currentFolder != null)
					dialogOpen.setCurrentFolder(currentFolder);
				dialogOpen.show();
				ResponseType what = dialogOpen.run();
				dialogOpen.hide();

				if (what == ResponseType.OK) {
					System.out.println("OK" + dialogOpen.getFilename());
					currentFolder = dialogOpen.getCurrentFolder();
					try {
						tourismQueries = new QFileParser(dialogOpen.getFilename());

						List<String> codes = tourismQueries.getCodes();
						textBufferCode.setText(codes.get(0));

						listStoreQueryNames.clear();

						/*
						 * Fill the list with all query names, select the first element, and
						 * add an event handler to load the code into the source text view.
						 */
						TreeIter row;
						for (String section : tourismQueries.getSections()) {
							row = listStoreQueryNames.appendRow();
							listStoreQueryNames.setValue(row, sectionName, section);
						}

						TreePath firstElement = listStoreQueryNames.getPath(listStoreQueryNames.getIterFirst());
						treeViewQueryNames.setCursor(firstElement, vertical, false);

						treeViewQueryNames.connect(new TreeView.RowActivated() {
							@Override
							public void onRowActivated(TreeView source, TreePath path, TreeViewColumn vertical) {
								int rowPos = Integer.parseInt(path.toString());
								textBufferCode.setText(codes.get(rowPos));
								textBufferSQL.setText("");
								textBufferInfo.setText("");
							}
						});
					} catch (IOException e) {
						e.printStackTrace();
					}
				}

			}

		});

		window.connect(new Window.DeleteEvent() {
			@Override
			public boolean onDeleteEvent(Widget source, Event event) {
				Gtk.mainQuit();
				return false;
			}
		});

		buttonDialog.connect(new Clicked() {

			@Override
			public void onClicked(Button arg0) {
				dialog.hide();
			}
		});

		Gtk.main();
	}

	private static void emptyTable() {
		for (TreeViewColumn tvc : treeviewTable.getColumns()) {
			treeviewTable.removeColumn(tvc);
		}
		if (listStoreTable != null)
			listStoreTable.clear();
	}

	private static void createTable(List<String> columnNames, Map<List<String>, Integer> groupings, String code) {

		emptyTable();

		int columnCount = columnNames.size();

		dataColumns = new DataColumn[columnCount + 1];

		for (int i = 0; i < columnCount; i++) {
			dataColumns[i] = new DataColumnString();
		}
		dataColumns[dataColumns.length - 1] = new DataColumnInteger();

		listStoreTable = new ListStore(dataColumns);

		TreeIter row;
		for (Entry<List<String>, Integer> e : groupings.entrySet()) {
			int i = 0;
			row = listStoreTable.appendRow();
			for (String v : e.getKey()) {
				if (! buttonIRI.getActive()) {
					v = QFileParser.replaceLongIRIs(v, code);
					v = QFileParser.replaceLiteralDatatype(v);
				}
				listStoreTable.setValue(row, (DataColumnString) dataColumns[i], v);
				i++;
			}
			listStoreTable.setValue(row, (DataColumnString) dataColumns[i], e.getValue().toString());
			listStoreTable.setValue(row, (DataColumnInteger) dataColumns[i + 1], e.getValue());
		}

		listStoreTable.setSortColumn(dataColumns[dataColumns.length - 1], SortType.DESCENDING);
		treeviewTable.setModel(listStoreTable);

		for (int i = 0; i < columnCount; i++) {
			TreeViewColumn vertical = treeviewTable.appendColumn();
			vertical.setTitle(String.format("%2d: %s", i, columnNames.get(i)));
			vertical.setResizable(true);

			CellRendererText renderer = new CellRendererText(vertical);
			renderer.setText((DataColumnString) dataColumns[i]);
		}

	}
}
