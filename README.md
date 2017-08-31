# tev's homemade pastebin
this is a product of me getting annoyed at having no good way of quickly sharing little pieces of text (or files!) across computers in my lab, without logging into my email everywhere. think of this like a worse version of GitHub Gist (*but* on your own network, and you get to control your tokens!).

### how to use this thing:
1. go to `<your_server>:8080/mystuff`, write things, paste things, upload things, cmd+enter
2. __done.__

### how to run this thing without compiling:
1. install docker-compose (`pip install docker-compose` or docker toolbox)
2. `cd deploy`
3. docker-compose up
4. it might work

### how to compile this thing (optional):
1. install docker toolbox
2. package into jar using maven (`mvn package`), copy jar into deploy folder
3. cd into deploy folder
4. do `docker-compose up`
5. it might work

### version
0.1? ___do not trust this thing guys___ it will break.

### license
MIT. go ahead, make millions with my code.
