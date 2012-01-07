@echo off
cd classes
for %%X in (..\test\LegalPrograms\*.ic) do ( 
	echo %%X
	java IC.Compiler %%X -L..\test\libic.sig -print-ast > %%X.out
)
for %%X in (..\test\IlegalPrograms\*.ic) do ( 
	echo %%X
	java IC.Compiler %%X -L..\test\libic.sig -print-ast > %%X.out
)
cd ..\test
move /Y LegalPrograms\*.out ourOut
move /Y IlegalPrograms\*.out ourOut
cd ..
