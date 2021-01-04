library(ggplot2)
library(dplyr)
library(reshape2)
files <- list.files("../output", recursive=FALSE)
setwd("plots")
for (file in files){

recPackets <- read.csv(paste("../../output/",file, sep = ""))
lim <- 300000

plot <- ggplot(recPackets, aes(x=tickCount, y=avgLatency))+geom_line()+theme_classic()+
  scale_x_continuous(breaks=seq(0, lim, lim/5), labels=seq(0, lim/1000, lim/5000))+
  xlab("Simulation Time (s)")+ylab("Average Latency (ms)")+
  theme(text = element_text(size = 18)) 
ggsave(plot, device="pdf", filename=paste(file,"_latency.pdf", sep=""),
       width = 8, height = 5, units = "in")

recPackets$cuts <- cut(x = recPackets$tickCount, breaks = seq(0, lim, lim/5))
plot <- ggplot(recPackets, aes(x=cuts, y=latency))+geom_boxplot()+
  scale_x_discrete(labels=seq(0, lim/1000, lim/5000))+
  xlab("Simulation Time (s)")+ylab("Latency")+
  theme(text = element_text(size = 18)) 
ggsave(plot, device="pdf", filename=paste(file,"_boxplot.pdf", sep=""),
       width = 8, height = 5, units = "in")

}

