import StartNode from './StartNode';
import EndNode from './EndNode';
import InteractionNode from './InteractionNode';
import LLMNode from './LLMNode';
import LogicNode from './LogicNode';
import DataNode from './DataNode';

const nodeTypes = {
  startNode: StartNode,
  endNode: EndNode,
  interactionNode: InteractionNode,
  llmNode: LLMNode,
  logicNode: LogicNode,
  dataNode: DataNode,
};

export default nodeTypes;