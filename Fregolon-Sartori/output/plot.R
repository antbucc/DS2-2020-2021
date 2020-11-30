library(ggplot2)
library(dplyr)
library(reshape2)

Repast_output <- read.csv("Repast_output.csv")
ReceivedPerturbations <- read.csv("ReceivedPerturbations.csv")
Total <- read.csv("TotalPerturbations.csv")

per <- Total
per <- filter(per, type=="Perturbation")
per <- group_by(per, id, sender, ref)
per <- summarise(per, n=n())
per <- group_by(per, id)
per <- summarise(per, avg=mean(n))
hist <- ggplot(per)+geom_histogram(aes(x=id, y=avg), stat = "identity")+theme_classic()+
  xlab("Id of the node")+ylab("Avg. receiving time")
ggsave(hist, filename = "hist.pdf", width = 8, height = 5, dpi = 300)

drop <- ggplot(Repast_output, aes(x=tick, y=DiscardedPackets))+geom_line()+
    theme_classic()+scale_y_continuous(limits=c(0,1))+
    ylab("Drop Rate")+xlab("Tick Count")
ggsave(drop, filename = "drop.pdf", width = 8, height = 5, dpi = 300)
repast <- ggplot(Repast_output, aes(x=tick, y=AvgLatency))+geom_line()+theme_classic()+ylab("Average Latency")+xlab("Tick Count")
ggsave(repast, filename = "repast.pdf", width = 8, height = 5, dpi = 300)


per <- ReceivedPerturbations
per <- filter(per, type=="Perturbation")
per <- mutate(per, cuts = cut(tickCount, seq(from=0, to=400000, by=2000)))
per <- group_by(per,cuts)
per <- summarise(per, Max=max(latency), Average=mean(latency), Min=min(latency))
per$id <- seq(1, length(per$cuts))
l <- length(per$cuts)
per <- melt(per, id=c("cuts","id"))
latency <- ggplot(per, aes(x=id, y=value, linetype=factor(variable)))+geom_line()+theme_classic()+
  xlab(expression("Tick count"))+ylab("Average Latency")+labs(linetype = "Legend")+
  scale_x_continuous(breaks=seq(0, l, l/5), labels = seq(0, 400000, 400000/5))+
  theme(axis.text.x = element_text(angle = 90))
ggsave(latency, filename = "latency.pdf", width = 8, height = 5, dpi = 300)

