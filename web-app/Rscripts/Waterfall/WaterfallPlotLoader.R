###########################################################################
# Copyright 2008-2012 Janssen Research & Development, LLC.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
###########################################################################

###########################################################################
#Waterfall Plot
###########################################################################

WaterfallPlot.loader <- function
(
	input.filename='outputfile',
	concept='',
	aggregate.probes = FALSE,
	output.file ="Waterfall"
)
{
	library(ggplot2)
	library(Cairo)
	
	#Pull the data for the histogram.
	line.data<-read.delim(input.filename,header=T)

	#Order the data by the x column.
	line.data <- line.data[order(line.data$X),]

	#Add an id column so we can place the boxes on the graph.
	line.data$id <- seq_along(line.data$PATIENT_NUM)

	#Make the patient_number column numeric.
	line.data$PATIENT_NUM <- as.character(line.data$PATIENT_NUM)

	#Make the Y axis pretty.
	yAxisLabel <- sub(pattern="^\\\\(.*?\\\\){3}",replacement="",x=concept,perl=TRUE)

	#Remove spaces from fill column.
	line.data$GROUP <- gsub("^\\s+|\\s+$", "",line.data$GROUP)
	
	#Set the fill column factor to have the correct order (follows the scale_fill_manual below).
	line.data$GROUP <- factor(line.data$GROUP, levels = c("BASE","LOW","HIGH"))

	#Set up the basic plot.
	tmp <- ggplot(line.data, aes(PATIENT_NUM,fill=GROUP)) 
	
	#Add the bars.
	tmp <- tmp + geom_rect(aes(x = PATIENT_NUM,ymin = 0, ymax = X,xmin=id - .45, xmax = id + .45))
	
	#Tilt the axis labels.
	tmp <- tmp + theme(axis.text.x = element_text(size = 13,angle=90))
	
	#Set the x label.
	tmp <- tmp + xlab('Patient Identifier') 
	
	#Set the y label.
	tmp <- tmp + ylab(yAxisLabel) 
	
	#Set the scale color manually.
	tmp <- tmp + scale_fill_manual(values = c("black","red","blue"))	
	
	CairoPNG(file=paste(output.file,".png",sep=""),width=800,height=800)
	
	print(tmp)
	
	dev.off()

}
