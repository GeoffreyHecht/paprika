require 'csv'

alls = CSV.read(ARGV[0])
aps = CSV.read(ARGV[1])

#subhead = aps[0].drop(1)
column_name = aps[0][1]
aps = aps.drop(1)

CSV.open('complete.csv', 'w') do |info|
	#info << alls[0].map{|x| x[2..-1]} + ['nb_ap']
	info << alls[0] + [column_name]
	alls = alls.drop(1)
	alls.each do |row|
		res = aps.detect do |data|
			row[0] == data[0]
		end
		if res.nil?
			info << row + [""]
		else
			info << row + [res[1]]
		end
	end
end