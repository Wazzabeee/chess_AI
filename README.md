<p float="left">
  <img src="https://github.com/Wazzabeee/chess_AI/blob/main/images/wall_e.PNG" />
</p>

# Chess Engine : PVS
## About

This chess AI was developed by [Justin Aubin](https://github.com/Justin-Aubin) and myself for an end-of-term project part of 8INF878 : AI course at UQAC. You can find the written report about this project (in French) in the repo.

It uses the [UCI](http://wbec-ridderkerk.nl/html/UCIProtocol.html) protocol. We implemented : 
- principal variation splitting (distributed algorithm)
- depth limited minimax with fail-soft alpha-beta pruning
- material evaluation
- tapered evaluation
- piece square tables
- transposition table
- quiescent search
- move ordering 
- opening book (20 000 games)

It can deliver a move in less than a second at depth 5 on most computers. If your computer has lot of cores, you may want to try put the depth parameter to 7 and modify the 1 second time limit in Timer.java.

Thanks to it we won the small tournament organized between the IA of the students of the class.

This has been possible thanks to [Chesslib](https://github.com/bhlangonijr/chesslib) for the chess rules and [Carballo](https://github.com/albertoruibal/carballo) for the reading of Polyglot files.

## How to use
The repo is a Maven project. Use the IDE of your choice to play with it. 
You can play against the AI using the console and [UCI](http://wbec-ridderkerk.nl/html/UCIProtocol.html) commands or build a .JAR file and use it on a Graphical User Interface supporting UCI protocol : [Arena](http://www.playwitharena.de/).

## Stats on Chess.com

<p float="left">
  <img src="https://github.com/Wazzabeee/chess_AI/blob/main/images/details.PNG" />
</p>

Games history can be found [here](https://www.chess.com/member/8inf878).
