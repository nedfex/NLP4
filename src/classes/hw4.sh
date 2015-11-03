javac -d ../classes ../*/*/*.java;
java -server -mx500m nlp.assignments.WordAlignmentTester -path ../../data4 -model baseline -data miniTest -verbose;