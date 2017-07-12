# MAWPS : A Math Word Problem Repository

This is the source code for setting up the MAWPS repository described in

  MAWPS: A Math Word Problem Repository.  
  Rik Koncel-Kedziorski**, Subhro Roy**, Aida Amini, Nate Kushman and Hannaneh Hajishirzi.  
  NAACL 2016 (Short).  

The system is live at http://lang.ee.washington.edu/MAWPS/.


### Creating Low Lexical and Template Overlap Subsets from the Terminal

 You will need to install Maven.
 
 Once you cloned the repo, run the following commands under
 dataset/ folder :

 1. mvn dependency:copy-dependencies (Downloads dependencies)

 2. mvn compile (Compiles java server code)

 3. java -cp target/classes:target/dependency/* server.MaxCoverage <questions_file> <num_output_questions> <reduce_lexical_overlap (true/false)> <reduce_template_overlap (true/false)> <output_file (optional)>

Currently we do not support check for grammatical errors from the terminal. The questions file must have all the questions arranged in the following json format. 
~~~~
[
  {
    "iIndex": 1,
    "sQuestion": "Joan found 70.0 seashells on the beach . She gave Sam some of her seashells . She has 27.0 seashells . How many seashells did she give to Sam ?",
    "lEquations": [
      "X=(70.0-27.0)"
    ],
    "lSolutions": [
      43.0
    ]
  },
  
...
]
~~~~
iIndex is a unique integer identifier for a problem, sQuestion is the problem text, lEquations is the list of equations, and lSolutions is the list of answers.


### Setting up MAWPS

 First, you will need to install
 1. Maven  (for server code)
 2. PHP 5.4.0 or higher  (for the web server)
 3. MySql database
 
 Once you cloned the repo, run the following commands under
 dataset/ folder :

 1. mvn dependency:copy-dependencies (Downloads dependencies)

 2. mvn compile (Compiles java server code)

 3. sh startServer.sh  (starts java xmlrpc server)

 4. php -S localhost:5000 (starts webserver, should be replaced by a 
    better web server for deployment)

 5. Start MySQL database with 

    mysql.server start

 6. To initialize database, run

    sh initDatabase.sh
    
  7. Download the ace parser (Linux x64 Binaries can be found here 
  	 http://sweaglesw.org/linguistics/ace/ ) and the ERG 1214 grammar file 
  	 (same link). Check the command in GrammarCheck.java for exact files
  	 needed and their location.	  

 The server is now live, and can accept questions, and show existing 
 questions. Open your browser and migrate to 

 localhost:5000/src/main/java/server/index.php   



