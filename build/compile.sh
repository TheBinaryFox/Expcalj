#!/bin/bash
cd "$(cd $(dirname "${BASH_SOURCE[0]}") | pwd)"

#
# Compile
compile() {
	local sp=""
	for arg in "$@"
	do
		if [ "$sp" = "" ]; then
			sp="$arg"
		else
			sp="$sp:$arg"
		fi
	done
	
	for arg in "$@"
	do
		compile_do "$sp" "$arg" "${#arg}"
	done
}
#
# Do the compiling.
compile_do() {
	for file in "$2"/*
	do
		if [ "$file" = "." ] || [ "$file" = ".." ]; then
			continue
		fi
		
		# Go through child directory.
		if [ -d "$file" ]; then
			compile_do "$1" "$file" "$3"
			continue
		fi
		
		# Compile file.
		if [[ "$file" = *.java ]]; then
			local package="$(dirname "${file:$(($3+1))}")"
			local name="$(basename "$file")"
			local class="${name%.*}"
			package="${package//\//.}"
			
			printf "\033[2m%s.\033[0m%s\033[0m\n" "$package" "$class"
			
			javac -sourcepath "$1" -d "tmp-bin" "$file" >> "tmp-log/compile.log" 2>&1
		fi
	done
}

#
# Prepare for building.
printf "\033[33mPreparing...\033[0m\n"
if [ -f "expcalj.jar" ]; then
	rm "expcalj.jar"
fi

#
# Compile.
mkdir tmp-bin
mkdir tmp-log
printf "\033[33mCompiling...\033[0m\n"
compile "../src" "../demo" "../lib/BigDecimalMath"

#
# Write META-INF.
mkdir tmp-bin/META-INF
cat << END > "tmp-bin/META-INF/MANIFEST.MF" 
Manifest-Version: 1.0
Main-Class: expcalj.cli.ExpcaljProgram
END

#
# Compress into archive.
printf "\033[33mCompressing...\033[0m\n"
cd tmp-bin
zip --quiet -r "../expcalj.jar" *
cd ../

#
# Clean up.
printf "\033[33mCleaning...\033[0m\n"
rm -rf "tmp-bin"
rm -rf "tmp-log"