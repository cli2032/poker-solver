# Kuhn Poker Solver and Visualizer

A program for visualizing how an algorithm learns to play optimal poker.

## Overview

This is a Java program which approximates nash equilibrium strategies in a simplified variant of poker known as Kuhn Poker. As per [the game's wikipedia entry](https://en.wikipedia.org/wiki/Kuhn_poker), a game of Kuhn poker proceeds as follows:

* Each player antes 1.
* Each player is dealt one of the three cards (a jack, a queen, or a king), and the third is put aside unseen.
* Player one can check or bet 1.
    * If player one checks then player two can check or bet 1.
        * If player two checks there is a showdown for the pot of 2 (i.e. the higher card wins 1 from the other player).
        * If player two bets then player one can fold or call.
            * If player one folds then player two takes the pot of 3 (i.e. winning 1 from player 1).
            * If player one calls there is a showdown for the pot of 4 (i.e. the higher card wins 2 from the other player).
    * If player one bets then player two can fold or call.
        * If player two folds then player one takes the pot of 3 (i.e. winning 1 from player 2).
        * If player two calls there is a showdown for the pot of 4 (i.e. the higher card wins 2 from the other player).

In order to generate a game theory optimal strategy for Kuhn Poker, this program implements a counterfactual regret minimization algorithm, which is commonly used for solving games of imperfect information. The algorithm works by starting out with an essentially random strategy (i.e, play every possible action in every possible decision point with completely equal chance) and then evaluates how much it won or lost as a result of taking each action. Based off of that result, the algorithm then adjusts its strategy in the next iteration of the game, taking actions with a frequency that is inversely proportional to their regret towards that action in prior iterations: Actions that resulted in higher payouts (low regret) are played at a higher frequency, and actions that resulted in lower payouts or bigger losses (high regret) are played at a lower frequency. Over the course of many iterations, the generated strategy will approach theoretical unexploitability.

When run, this program will print out the expected value of the final generated strategy for the two players, as well as the final frequencies that each action should be played at each possible decision point. The output of the generated strategies is formatted such that there is first a number, representing which card the current player has, followed by zero or more letters representing the sequence of actions that have been taken thus far - a `b` represents a bet or a call (more money was put into the pot), and a `p` represents a check or a fold (no money was put into the pot). As an example, the output `1pb` tells us the current player is holding the `1` card (our representation of the Jack), and that so far there has been a check from player 1 and a bet from player 2. Therefore, we know that player 1 is the one now making the decision between calling and folding. The first value of the array that follows each game state contains the frequency that the strategy checks/folds at that node, and the second value contains the frequency that the strategy bets/calls. Given that a player only ever has two options at any decision node, if one of these actions has frequency `a`, then the other action at that node must necessarily have frequency `1 - a`.

The program has also been configured to generate a line graph showing how frequencies of different betting actions change over the course of the programs iterations. From this, we can see how the program quickly converges towards correct strategies. 

![alt text](https://github.com/cli2032/poker-solver/tree/main/kuhnpokersolver/1000iterations.png?raw=true)

For instance, in the following image we see that the program learns to always bet and call in the `3p` and `3pb` line, respectively. This makes intuitive sense: when we have the `3` (King), we have the best possible hand, so when we are the last to act, whether facing a bet or deciding whether or not to bet, we should always opt to put more money into the pot, as we are guaranteed to win it. Following the same logic, we see the program also correctly learns to always fold when facing a bet and holding the `1` (Jack), since it is guaranteed to lose at showdown. Moreover, when holding the `2` (Queen), the program quickly opts to never bet when given the option, as when a player bets when holding the Queen, their opponent will always fold when they have an inferior hand and always call when they have a superior one.

Users can opt to set the `totalIterations` variable to different values in order to see how outputs change as the number of iterations increases, although currently this value must be at least 20 for the program to work and be displayed properly.
