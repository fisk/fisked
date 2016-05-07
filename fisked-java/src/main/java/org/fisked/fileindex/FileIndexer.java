/*******************************************************************************
 * Copyright (c) 2016, Erik Österlund
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the organization nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL ERIK ÖSTERLUND BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *******************************************************************************/
package org.fisked.fileindex;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.store.FSDirectory;

public class FileIndexer {
	private static StandardAnalyzer _analyzer = new StandardAnalyzer();

	private final IndexWriter _writer;
	private final ArrayList<File> _queue = new ArrayList<File>();
	private final String _indexLocation;

	public List<Document> searchContent(String text) throws IOException, ParseException {
		List<Document> result = new ArrayList<>();
		IndexReader reader = DirectoryReader.open(FSDirectory.open(Paths.get(_indexLocation)));
		IndexSearcher searcher = new IndexSearcher(reader);
		TopScoreDocCollector collector = TopScoreDocCollector.create(128);

		Query q = new QueryParser("contents", _analyzer).parse(text);
		searcher.search(q, collector);
		ScoreDoc[] hits = collector.topDocs().scoreDocs;

		for (ScoreDoc hit : hits) {
			int docId = hit.doc;
			Document document = searcher.doc(docId);
			result.add(document);
		}
		return result;
	}

	public List<Document> searchPath(String text) throws IOException, ParseException {
		List<Document> result = new ArrayList<>();
		IndexReader reader = DirectoryReader.open(FSDirectory.open(Paths.get(_indexLocation)));
		IndexSearcher searcher = new IndexSearcher(reader);
		TopScoreDocCollector collector = TopScoreDocCollector.create(128);

		Query q = new QueryParser("path", _analyzer).parse(text);
		searcher.search(q, collector);
		ScoreDoc[] hits = collector.topDocs().scoreDocs;

		for (ScoreDoc hit : hits) {
			int docId = hit.doc;
			Document document = searcher.doc(docId);
			result.add(document);
		}
		return result;
	}

	public FileIndexer(String indexDir) throws IOException {
		FSDirectory dir = FSDirectory.open(Paths.get(indexDir));
		IndexWriterConfig config = new IndexWriterConfig(_analyzer);
		_writer = new IndexWriter(dir, config);
		_indexLocation = indexDir;
	}

	public void indexFileOrDirectory(String fileName) throws IOException {
		addFiles(new File(fileName));

		for (File f : _queue) {
			FileReader fr = null;
			try {
				Document doc = new Document();
				fr = new FileReader(f);
				doc.add(new TextField("contents", fr));
				doc.add(new StringField("path", f.getPath(), Field.Store.YES));
				doc.add(new StringField("filename", f.getName(), Field.Store.YES));

				_writer.addDocument(doc);
			} catch (Exception e) {
				System.out.println("Could not add: " + f);
			} finally {
				fr.close();
			}
		}

		_queue.clear();
		closeIndex();
	}

	private void addFiles(File file) {
		if (!file.exists()) {
			return;
		}
		if (file.isDirectory()) {
			for (File f : file.listFiles()) {
				addFiles(f);
			}
		} else {
			_queue.add(file);
		}
	}

	public void closeIndex() throws IOException {
		_writer.close();
	}
}
