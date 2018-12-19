if [ $# -lt 1 ]; then
	echo 'Invalid syntax, expected at least 1 argument'
	exit 1
else
	while test $# -gt 0; do
        	case "$1" in
	        	        -h|--help)
					echo "$0 - Sums the specified arugments, takes at least 2 arguments"
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
						if [ $# -lt 2 ]; then
							echo 'Invalid syntax, expected at least 2 integer arguments'
							exit 1
						else
							args=("$@")
							res=${args[0]}
							total=${args[0]}
							for arg in ${args[@]:1}; do
								res=$res' + '$arg
								total=$(( $total + arg ))
							done
							echo $res ' = ' $total
							exit 0
						fi
					fi
				;;
		esac
	done
fi

