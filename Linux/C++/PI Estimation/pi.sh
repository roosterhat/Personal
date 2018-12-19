in=0
count=0
size=500
#if [ $# -eq 1 ]; then
#	size=$1
#fi

timestamp(){
	echo $(date +%s)
}



tm=$(( 100 + $(timestamp) ))
while [ $(timestamp) -lt $tm ]; do
	x=$( awk -v seed=$RANDOM 'BEGIN{srand(seed);print rand()}' )
	y=$( awk -v seed=$RANDOM 'BEGIN{srand(seed);print rand()}' )
	if (( $(bc <<< "scale=10; sqrt($x^2 + $y^2) <= 1") ));then
		in=$(( $in+1 ))
	fi
	count=$(( $count+1 ))		
done
echo $count,$( bc <<<  "scale=10; 4*$in/$count" )
