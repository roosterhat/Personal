function FtoC(){
	local  result=`echo  "scale = 10;($1 - 32) * 5 / 9" | bc`
	echo $result
}

function KtoC(){
	local  result=`echo "scale = 10;$1 - 273" | bc`
	echo $result
}

function CtoF(){
	local  result=`echo  "scale = 10;$1 * 9 / 5 + 32" | bc`
	echo $result
}

function CtoK(){
	local  result=`echo  "scale = 10;$1 + 273" | bc`
	echo $result
}


if [ $# -lt 1 ]; then
	echo "Invalid syntax, expected at least 1 argument"
	exit 1
else
	while test $# -gt 0; do
	        case "$1" in
		                -h|--help)
					echo "$0 - converts temperature values between different scales, default (Fahrenheit to Celcius) "
					echo ""
					echo "$0 [options] [arguments]"
					echo ""
					echo "Usage:"
					echo "Specify the input and output scales followed by the temperature"
					echo "Example: '-fc 32' -> '0 ˚C'"
					echo ""
					echo "options:"
					echo "-h, --help	Displays script information"
					echo "-f 		Specifies Fahrenheit"
					echo "-c		Specifies Celcius"
					echo "-k		Specifies Kelvin"	
					exit 0
				;;
				 -[fck][fck])
				 	if [[ $# -lt 2 ]]; then
						echo "Invalid syntax, no temperature specified"
						exit 1
					else
						in=${1:1:1}
						out=${1:2:2}
						val=$2
						pat="(-?[0-9]+)(\.[0-9]+)?"
						if [[ ! $2 =~ $pat ]]; then
							echo "Invalid syntax, invalid temperature"
							exit 1
						else
							res=$([ $in == 'f' ] && echo $(FtoC $val) || echo $([ $in == 'k' ] && echo $(KtoC $val) || echo $val))
							res=$([ $out == 'f' ] && echo $(CtoF $res) || echo $([ $out == 'k' ] && echo $(CtoK $res) || echo $res))
							echo `printf '%.2f' $res | sed 's/-0.00/0.00/g'`" ˚"`echo $out | tr a-z A-Z`
							exit 0
						fi
					fi
				;;
				*)
					if [[ $1 == ?(-)+([0-9]) ]]; then 
						echo `printf '%.2f' $(FtoC $1) | sed 's/-0/0/g'`" ˚C"
						exit 0
					else
						echo "Unknown Command: "$1", -h for help"
						exit 1
					fi
				;;
		esac
	done
fi


