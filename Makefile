V2VM: 	V2VM.java
		javac -cp "./:./vapor-parser.jar" V2VM.java
		java -cp "./:./vapor-parser.jar" V2VM < P.vapor > P.vaporm

test: V2VM.java 
	mkdir hw3
	cp Find.java Graph.java Interval.java Node.java NodeList.java Register.java Scan.java Visitor.java V2VM.java hw3/
	tar zcf hw3.tgz hw3
	rm -rf hw3
	chmod u+x Phase3Tester/run
	Phase3Tester/run Phase3Tester/SelfTestCases hw3.tgz

run1: V2VM.java 
	javac -cp "./:./vapor-parser.jar" V2VM.java
	java -cp "./:./vapor-parser.jar" V2VM < Phase3Tester/SelfTestCases/1-Basic.vapor > vaporm_files/Basic.vaporm
	java -jar vapor.jar run -mips vaporm_files/Basic.vaporm

run2: V2VM.java 
	javac -cp "./:./vapor-parser.jar" V2VM.java
	java -cp "./:./vapor-parser.jar" V2VM < Phase3Tester/SelfTestCases/2-Loop.vapor > vaporm_files/Loop.vaporm
	java -jar vapor.jar run -mips vaporm_files/Loop.vaporm

run3: V2VM.java 
	javac -cp "./:./vapor-parser.jar" V2VM.java
	java -cp "./:./vapor-parser.jar" V2VM < Phase3Tester/SelfTestCases/BinaryTree.vapor > vaporm_files/BinaryTree.vaporm
	java -jar vapor.jar run -mips vaporm_files/BinaryTree.vaporm

run4: V2VM.java 
	javac -cp "./:./vapor-parser.jar" V2VM.java
	java -cp "./:./vapor-parser.jar" V2VM < Phase3Tester/SelfTestCases/BubbleSort.vapor > vaporm_files/BubbleSort.vaporm
	java -jar vapor.jar run -mips vaporm_files/BubbleSort.vaporm

run5: V2VM.java 
	javac -cp "./:./vapor-parser.jar" V2VM.java
	java -cp "./:./vapor-parser.jar" V2VM < Phase3Tester/SelfTestCases/Factorial.vapor > vaporm_files/Factorial.vaporm
	java -jar vapor.jar run -mips vaporm_files/Factorial.vaporm

run6: V2VM.java 
	javac -cp "./:./vapor-parser.jar" V2VM.java
	java -cp "./:./vapor-parser.jar" V2VM < Phase3Tester/SelfTestCases/LinearSearch.vapor > vaporm_files/LinearSearch.vaporm
	java -jar vapor.jar run -mips vaporm_files/LinearSearch.vaporm

run7: V2VM.java 
	javac -cp "./:./vapor-parser.jar" V2VM.java
	java -cp "./:./vapor-parser.jar" V2VM < Phase3Tester/SelfTestCases/LinkedList.vapor > vaporm_files/LinkedList.vaporm
	java -jar vapor.jar run -mips vaporm_files/LinkedList.vaporm

run8: V2VM.java 
	javac -cp "./:./vapor-parser.jar" V2VM.java
	java -cp "./:./vapor-parser.jar" V2VM < Phase3Tester/SelfTestCases/MoreThan4.vapor > vaporm_files/MoreThan4.vaporm
	java -jar vapor.jar run -mips vaporm_files/MoreThan4.vaporm

run9: V2VM.java 
	javac -cp "./:./vapor-parser.jar" V2VM.java
	java -cp "./:./vapor-parser.jar" V2VM < Phase3Tester/SelfTestCases/QuickSort.vapor > vaporm_files/QuickSort.vaporm
	java -jar vapor.jar run -mips vaporm_files/QuickSort.vaporm

run10: V2VM.java 
	javac -cp "./:./vapor-parser.jar" V2VM.java
	java -cp "./:./vapor-parser.jar" V2VM < Phase3Tester/SelfTestCases/TreeVisitor.vapor > vaporm_files/TreeVisitor.vaporm
	java -jar vapor.jar run -mips vaporm_files/TreeVisitor.vaporm

clean:
	rm -rf *.class
	rm -rf *.tgz
	