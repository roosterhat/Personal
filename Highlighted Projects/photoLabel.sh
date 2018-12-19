fileTypes=".jpg .png"
files=()
format="%S"
func=""
longest=0
errors=()
rename=true
extension=true

function getFiles(){
	local fileIn=()
	while [ $# -gt 0 ]; do
		if [ -f $1 ]; then
			fileIn+=($1)
		elif [ -d $1 ]; then
			for file in $1*; do
				if [ -f "$file" ]; then 
					fileIn+=($file)
				elif [ -d "$file" ]; then
					fileIn=($fileIn $(getFiles "$file"))
				fi 
			done
		else
			errors+=($1)
		fi
		shift 1
	done
	 echo ${fileIn[@]}
}

function replaceConstants(){
	tmp=$(echo "$1"   | sed "s,{PATH},$2,")
	name=$(if [[ "$3" =~ "." ]]; then echo ${3:0:$(echo $3 | grep -b -o "\." | cut -f 1 -d :)}; else echo $3; fi)
	tmp=$(echo "$tmp" | sed "s,{NAME},$name,")
	tmp=$(echo "$tmp" | sed "s,{COUNT},$4,")
	echo "$tmp"
}

function getFileExtension(){
	base=$(basename $1)
	if [ $extension = true ] && [[ "$base" =~ "." ]]; then
		echo ${base:$(echo $base | grep -b -o "\." | cut -f 1 -d :)}
	else
		echo ""
	fi
}


if [ $# -lt 1 ]; then
	echo "Invalid Syntax, expected at least 1 argument (use --help for usage information)"
	exit 1
else
	while [ $# -gt 0 ]; do
	case "$1" in
			-h|--help)
				echo "$0 - formats the filenames of the photos (.jpg) based on the exif data"
				echo ""
				echo "$0 [options] [arguments]"
				echo ""
				echo "Usage:"
				echo "Specify the type of format and the photos or directory of the photos"
				echo "Example: '-s camera_roll myPhoto.jpg' -> 'camera_roll1"
				echo ""
				echo "options:"
				echo "-h, --help        			Displays script information"
				echo "-r					Only display files names, does not rename original file"
				echo "-e 					Does not append original file extension to new file name"
				echo "-d 					Specifies the date format (YYYYMMDD.jpg)"
				echo "-s 'string'				Specifies the string increment format ('string'###.jpg)"
				echo "-f 'format' 'feild' 'function'		Specifies a custom prtinf format, a exif field, and a function to parse exif data as input."
				echo "					All fields are interpreted as strings"
				echo " 					The function is piped stdin from 'identify -verbose {PATH} | grep 'field' | sed -e 's/^[[:space:]]*//' |'"
				echo "					where '{PATH}' is replaced with a file path during runtime."
				echo "					There are several markers that will be replaced during run time with a value."
				echo "						'{PATH}'  is replaced with the path of the file"
				echo "						'{NAME}'  is replaced with the name of the file"
				echo "						'{COUNT}' is replaced with the file index"
				echo "-c 'format' 'function'			Specifies a custom format and a function"
				echo "					Similar to '-f' but does not get piped any stdin"
				echo "					Use markers to get specific values during runtime"
				echo "					All fields are interpreted as strings"
				exit 0
			;;
			-d)
				format="%s%s%s"
				func='str=$(identify -verbose "{PATH}"| grep DateTimeOriginal | cut -f 6 -d " "| tr :- " "); if [[ $str =~ .*T.* ]]; then echo $(echo $str | cut -f 1 -d "T"); else echo $str; fi;'
				shift
				if [ $# -lt 1 ]; then
					echo "Invalid Syntax, no files or directories specified"
					exit 1
				fi
				files=($(getFiles $@)) 
				shift $#				
			;;
			-s)
				str=$2
				shift 2
				if [ $# -lt 1 ]; then
					echo "Invalid Syntax, no files or directories specified"
					exit 1
				fi
				files=($(getFiles $@))
				size=`echo "l(${#files[@]})/l(10)+1" | bc -l`
				size=`printf "%.0f" $size`
				format="$str%0"$size"d"
				func='echo {COUNT}'
				shift $#
			;;
			-f)
				format=$2
				func="identify -verbose "{PATH}"| grep $3 | sed -e 's/^[[:space:]]*//' |"$4
				shift 4
				if [ $# -lt 1 ]; then
					echo "Invalid Syntax, no files or directories specified"
					exit 1
				fi
				files=($(getFiles $@))
				shift $#
			;;
			-c)
				format=$2
				func=$3
				shift 3
				if [ $# -lt 1 ]; then
					echo "Invalid Syntax, no files or directories specified"
					exit 1
				fi
				files=($(getFiles $@))
				shift $#
			;;
			-r)
				rename=false
				shift 1			
			;;
			-e)
				extension=false
				shift 1
			;;
			*)
				echo "Invalid syntax, unknown command '$1'"
				exit 1
			;;
	esac
	done
	if [ ${#errors} -gt 0 ]; then
		echo "Syntax Errors in the following file/directory paths"
		for e in ${errors[*]}; do
			echo "	$e"
		done
		echo ""
	fi
	for f in ${files[*]}; do longest=$(($longest > ${#f} ? $longest : ${#f})); done
	echo "Format: "$format
	echo "Function: "$func
	count=0
	if [ ${#files} -eq 0 ];then
		echo "No files found"
		exit 1
	fi
	for f in ${files[*]}; do
		cmd="$(replaceConstants "$func" "$f" "$(basename $f)" "$count")"
		new=$(dirname $f)"/"$(printf $format $(eval $cmd))$(getFileExtension $f)
		if [ $rename = true ];then
			echo $(printf "%${longest}s -> %s" $f $new) 
			$(mv $f $new)
		else
			echo $new
		fi
		count=$((count+1))
	done

fi
