if [ $# -lt 1 ]; then
	echo "Invalid syntax: expected 1 integer argument"
	exit 1
else
	while test $# -gt 0; do
	        case "$1" in
		                -h|--help)
					echo "$0 - Preforms the factorial of the specified arugment"
					echo ""
					echo "$0 [options] [arugments]"
					echo ""
					echo "options:"
					echo "-h, --help	displays script information"
					exit 0
					;;
				*)
					if [[ $1 == ?(-)+([0-9]) ]]; then 
						val=$1
						total=1
						while [ $val -gt 0 ]; do
							total=$(($total * $val))
							let val=val-1
						done
						echo $1'! = '$total
						exit 0
					else
						echo "Unknown Command: "$1", -h for help"
						exit 1
					fi
				;;
		esac
	done
	
fi
