@echo off
cd classes
for %%X in (..\test\LegalPrograms\*.ic ..\test\LegalPrograms\*.txt) do ( 
	echo %%X
	java IC.Compiler "%%X" -L..\test\libic.sig -dump-symtab > "%%X.out"
)
for %%X in (..\test\IlegalPrograms\*.ic) do ( 
	echo %%X
	java IC.Compiler "%%X" -L..\test\libic.sig -dump-symtab > "%%X.out"
)
cd ..\test
move /Y LegalPrograms\*.out ourOut
move /Y IlegalPrograms\*.out ourOut
cd ..
