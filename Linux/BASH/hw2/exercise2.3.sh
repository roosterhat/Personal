if [ $# -lt 1 ];then
	echo "Invalid syntax, expected 1 integer argument"
	exit 1
else
	while test $# -gt 0; do
	        case "$1" in
		                -h|--help)
					echo "$0 - converts integer bases, defaults to decimal"
					echo " "
					echo "$0 [options] [arguments]"
					echo " "
					echo "options:"
					echo "-h, --help		Displays script information"
					echo "-d, -dec, --decimal	Converts the specified value to hexadecmial"
					echo "-H, -hex, --hexadecmial	Converts the specified value to decimal"
					exit 0
				;;
				-d|-dec|--decimal)
					shift
					echo $1'->'`echo "obase=16; "$1 | bc`
					exit 0
					;;
				-H|-hex|--hexadecimal)
					shift
					echo $1'->'`echo "ibase=16; "$1 | bc`
					exit 0
					;;
				*)
					if [[ $1 == ?(-)+([0-9]) ]]; then 
						echo $1'->'`echo "obase=16; "$1 | bc`
						exit 0
					else
						echo "Unknown Command: "$1", -h for help"
						exit 1
					fi
				;;
		esac
	done	
fi
