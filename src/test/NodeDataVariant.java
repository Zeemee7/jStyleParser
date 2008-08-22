package test;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.BitSet;
import java.util.Map;

import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.traversal.NodeFilter;
import org.w3c.tidy.Tidy;

import cz.vutbr.web.css.CSSFactory;
import cz.vutbr.web.css.CSSProperty;
import cz.vutbr.web.css.Declaration;
import cz.vutbr.web.css.NodeData;
import cz.vutbr.web.css.StyleSheetNotValidException;
import cz.vutbr.web.css.SupportedCSS;
import cz.vutbr.web.css.Term;
import cz.vutbr.web.csskit.parser.CSSParser;
import cz.vutbr.web.domassign.Analyzer;
import cz.vutbr.web.domassign.QuadrupleMapNodeData;
import cz.vutbr.web.domassign.SingleMapNodeData;
import cz.vutbr.web.domassign.TidyTreeWalker.Traversal;

@Ignore
public class NodeDataVariant {

	private static Logger log = Logger.getLogger(NodeDataVariant.class);

	private static SupportedCSS css = CSSFactory.getSupportedCSS();

	private static Document doc;
	private static Analyzer analyzer;

	@BeforeClass
	public static void init() throws FileNotFoundException,
			StyleSheetNotValidException {

		Tidy parser = new Tidy();
		parser.setCharEncoding(org.w3c.tidy.Configuration.UTF8);

		doc = parser.parseDOM(new FileInputStream("data/simple/data.html"),
				null);

		CSSParser cssparser = new CSSParser(new FileInputStream(
				"data/simple/data.css"));

		analyzer = new Analyzer(cssparser.parse());
	}

	@Test
	@Ignore
	public void testMemoryAllocationEmpty() throws Exception {

		long sm, qm, enumArray;

		sm = memoryUsage(SingleMapNodeData.class);
		qm = memoryUsage(QuadrupleMapNodeData.class);
		enumArray = memoryUsage(NodeDataEnumArray.class);

		log.debug("===\nMemory test:");
		log.debug("SingleMap size: " + sm
				+ " QuadrupleMap size: " + qm + " NodeDataEnumArray size: "
				+ enumArray);

	}

	@Test
	//@Ignore
	public void testTimeUsage() throws Exception {

		long sm, qm, enumArray;

		sm = timeUsage(SingleMapNodeData.class);
		qm = timeUsage(QuadrupleMapNodeData.class);
		enumArray = timeUsage(NodeDataEnumArray.class);

		log.debug("===\nTime test:");
		log.debug("QuadrupleMap time: " + qm + " SingleMap time: "
				+ sm + " NodeDataEnumArray time: " + enumArray);

	}
	
	@Test
	//@Ignore
	public void testInheritance() throws Exception {
		
		final int SAMPLE = 100;
		
		long sm = 0, smt = 0, qm = 0, qmt = 0, tmp = 0;
		
		// memory tests
		/*
		inheritance(SingleMapNodeData.class);
		tmp = memoryUsage();
		for(int i=0; i<SAMPLE; i++)
			inheritance(SingleMapNodeData.class);
		sm = memoryUsage();
		
		sm = Math.round((sm - tmp)/(float) SAMPLE);
		
		inheritance(QuadrupleMapNodeData.class);
		tmp = memoryUsage();
		for(int i=0; i<SAMPLE; i++)
			inheritance(QuadrupleMapNodeData.class);
		qm = memoryUsage();
		
		qm = Math.round((qm - tmp)/(float) SAMPLE);
		*/

		// time tests
		for(int i=0; i<SAMPLE; i++) {
			tmp = System.currentTimeMillis();
			inheritance(SingleMapNodeData.class);
			smt +=  System.currentTimeMillis() - tmp;
		}
		smt = Math.round(smt/((float) SAMPLE));
		
		for(int i=0; i<SAMPLE; i++) {
			tmp = System.currentTimeMillis();
			inheritance(QuadrupleMapNodeData.class);
			qmt += System.currentTimeMillis() - tmp;
		}
		qmt = Math.round(qmt/((float) SAMPLE));
		
		
		
		log.debug("===\nInheritance test:");
		log.debug("Quadruple map memory: " + qm + "B at " + qmt + 
				"ms SingleMap memory: " + sm + "B at " + smt +"ms");
		
	}
	

	private void inheritance(Class<? extends NodeData> clazz) {

		CSSFactory.registerNodeDataInstance(clazz);
		
		Map<Element, NodeData> declInh = analyzer.evaluateDOM(doc, "all", true);

		Map<Element, NodeData> decl = analyzer.evaluateDOM(doc, "all", false);

		Pair<Map<Element, NodeData>, Map<Element, NodeData>> pair = new Pair<Map<Element, NodeData>, Map<Element, NodeData>>(
				declInh, decl);

		Traversal<Boolean> traversal = new Traversal<Boolean>(doc,
				(Object) pair, NodeFilter.SHOW_ELEMENT) {
			@SuppressWarnings("unchecked")
			@Override
			protected void processNode(Boolean result, Node current,
					Object source) {

				Pair<Map<Element, NodeData>, Map<Element, NodeData>> pair = (Pair<Map<Element, NodeData>, Map<Element, NodeData>>) source;

				Element e = (Element) current;

				NodeData ndInh = pair.getFirst().get(e);
				NodeData nd = pair.getSecond().get(e);

				String id = e.getAttribute("id");

				if (id != null)
					Assert.assertNotSame("Inherited differs for #" + id, ndInh,
							nd);
			}

		};

		traversal.levelTraversal(null);		
	}
	
	
	

	private long timeUsage(Class<? extends NodeData> clazz) throws Exception {

		final int SAMPLE = 100000;

		CSSFactory.registerNodeDataInstance(clazz);

		long timeBefore = System.currentTimeMillis();
		for (int i = 0; i < SAMPLE; i++)
			CSSFactory.createNodeData();
		long timeAfter = System.currentTimeMillis();

		return timeAfter - timeBefore;
	}

	private long memoryUsage(Class<? extends NodeData> clazz) throws Exception {

		final int SAMPLE = 100;

		CSSFactory.registerNodeDataInstance(clazz);

		Object[] holder = new Object[SAMPLE];

		@SuppressWarnings("unused")
		Object throwAway = clazz.newInstance();

		long memoryUsageBefore, memoryUsageAfter;
		int i = 0;

		memoryUsageBefore = memoryUsage();
		for (i = 0; i < SAMPLE; i++)
			holder[i] = CSSFactory.createNodeData();
		memoryUsageAfter = memoryUsage();

		return Math.round((memoryUsageAfter - memoryUsageBefore)
				/ ((float) SAMPLE));

	}

	private long memoryUsage() throws Exception {

		final int SLEEP_TIME = 100;
		long memUsage;

		System.gc();
		Thread.sleep(SLEEP_TIME);
		System.runFinalization();
		Thread.sleep(SLEEP_TIME);
		System.gc();
		Thread.sleep(SLEEP_TIME);
		System.runFinalization();
		Thread.sleep(SLEEP_TIME);
		memUsage = Runtime.getRuntime().totalMemory();

		System.gc();
		Thread.sleep(SLEEP_TIME);
		System.runFinalization();
		Thread.sleep(SLEEP_TIME);
		System.gc();
		Thread.sleep(SLEEP_TIME);
		System.runFinalization();
		Thread.sleep(SLEEP_TIME);

		return memUsage - Runtime.getRuntime().freeMemory();
	}

	public static class NodeDataEnumArray implements NodeData {

		@SuppressWarnings("unused")
		private NodeData[] properties;
		@SuppressWarnings("unused")
		private Term<?>[] values;
		@SuppressWarnings("unused")
		private BitSet inherited;

		public NodeDataEnumArray() {
			this.properties = new NodeData[css.getTotalProperties()];
			this.values = new Term<?>[css.getTotalProperties()];
			this.inherited = new BitSet(css.getTotalProperties());
		}

		public <T extends CSSProperty> T getProperty(Class<T> clazz, String name) {
			return null;
		}

		public <T extends CSSProperty> T getProperty(Class<T> clazz,
				String name, boolean includeInherited) {
			return null;
		}

		public <T extends Term<?>> T getValue(Class<T> clazz, String name) {
			return null;
		}

		public <T extends Term<?>> T getValue(Class<T> clazz, String name,
				boolean includeInherited) {
			return null;
		}

		public NodeData push(Declaration d) {
			return this;
		}

		public NodeData inheritFrom(NodeData parent) {
			return this;
		}

		public NodeData concretize() {
			return this;
		}

	}
	
	static class Pair<T, V> {
		private T first;
		private V second;

		public Pair(T first, V second) {
			this.first = first;
			this.second = second;
		}

		public T getFirst() {
			return first;
		}

		public V getSecond() {
			return second;
		}

	}

}