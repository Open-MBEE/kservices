y = 2
for i in 0..len(arr):
  y += arr[i] * arr2[i]
z = y

or

y = 2 + sum(map(lambda x,y: x*y, arr, arr2))
z = y


var y : Seq[Real]
y[0] = 2 
forall i : Int :- i in 1..len(arr) ->
   y[i] = y[i-1] + arr[i] * arr2[i]
var z = y[len[arr]]



-- K protocol executive

class packet {
  var seqId : Int
  -- var channel : Int
  var data : Int
}

class Action -- extends DurativeEvent?

class resendRequest extends Action {
   -- var channel : Int
   var seqId : Int
}

class sendPacket extends Action {
   var packet: Packet
}

class Queue extends Seq[Packet] {
   var lastSeqId : Int
}

class Accept { -- extends DurativeEvent?
   var p : Packet
   var q : Queue
   var qNext : Queue
   var response : Seq[Action]

   req p.seqId > q.lastSeqId+1 =>
       forall id : Int :-
         id in lastSeqId+1..p.seqId =>
         response.contains(resendRequest(seqId::id))
   req p.seqId = q.lastSeqId+1 => response.contains(sendPacket(packet::p))
   req forall p1 : Packet :-
         (p1 in q &&
          exists p2 : Packet, s : sendPacket :-
              s in response && s.packet = p2 && p2.seqId = p1.seqId-1) =>
         response.contains(sendPacket(packet::p1))
   req qNext.lastSeqId = max(Seq{id : Int :- exists s : sendPacket :- s in response && s.packet.seqId = id})
   req forall pNext: Packet :-
         (pNext in q || pNext = p) && qNext.lastSeqId < pNext.seqId =>
         pNext in qNext
}

// closed loop execution in Java or something
void main() {
   Accept lastAccept = Accept();
   while (true) {
      Packet p = accept();  -- from network
      Accept a = new Accept(p);  -- instantiating from K model
      a.q = lastAccept.qNext;  -- update queue from last accept()
      a.satisfy();  -- solve problem
      execute( a.response );
      lastAccept = a;
   }
}







