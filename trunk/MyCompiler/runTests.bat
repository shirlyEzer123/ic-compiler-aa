@echo off
cd classes
for %%X in (..\test\Legal\*.ic ..\test\Legal\*.txt) do ( 
	echo %%X
	java IC.Compiler "%%X" -L..\test\libic.sig -dump-symtab > "%%X.out"
)
for %%X in (..\test\Ilegal\*.ic) do ( 
	echo %%X
	java IC.Compiler "%%X" -L..\test\libic.sig -dump-symtab > "%%X.out"
)
cd ..\test
move /Y Legal\*.out ourOut
move /Y Ilegal\*.out ourOut
cd ..
