if [ $# -lt 1 ]; then
	echo "Invalid syntax, expected at least 1 argument"
	exit 1
else
	while test $# -gt 0; do
       	 case "$1" in
		                -h|--help)
					echo "$0 - Finds the maximum value of specified arugments, takes atleast 3 arguments"
					echo ""
					echo "$0 [options] [arugments]"
					echo ""
					echo "options:"
					echo "-h, --help	displays script information"
					exit 0
					;;
				*)
					if [[ $1 == -+([a-zA-Z]) ]]; then 
						echo "Unknown Command: "$1", -h for help"
						exit 1
					else
						if [ $# -lt 3 ]; then
							echo 'Invalid syntax, expcted 3 at least integer arguments'
							exit 1
						else
							args=("$@")
							max=${args[0]}
							for arg in ${args[@]:1}; do
								max=$(( $max > $arg ? $max : $arg ))
							done
							echo 'Max: ' $max
							exit 0
						fi
					fi
				;;
		esac
	done
fi
