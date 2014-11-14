Information-Retrieval
=====================

The programs were created for Information retrieval class.
Written in java using lucene indexing and trec corpus as testing data.

assignment-1
------------
Three parts to the assignment
Parsing TREC-> I used an xml parser to parse all the trec doc in in corpus. 
Index-> Used lucene library to create index for the tokens extracted in previous step.
Different analyers->Used Keyword, Stopword, Standard and Simple analyzer in lucene library and compared
the indexing performance of each.

assignment-2
------------
Three parts to the assignment
Ranking-> Using the index obtained from previous assignment we use the TF-IDF to rank documents.
Easy Search-> We generate queries from the document given(the topic and description are passed as query to ranking function above).
Result is ranked result from based on TF-IDF.
Compare algorithms->Different searching models are compared like Dirichlet, vector space model, BM25, JM smoothing
