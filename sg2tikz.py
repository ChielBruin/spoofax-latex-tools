import argparse
import re


class Connection (object):
    def __init__(self, node, x, y, dir, data, endpoints):
        self.label = ''
        self.from_node = node
        self.from_node_side = self.dir_to_tikz_dir(dir)
        self.from_node.add_out_edge(self)

        self.to_node = None
        self.label = 'refdec'

        dir = self.update_dir(data[y][x], dir)
        while True:
            (x, y, dir) = self.follow_connection(data, x, y, dir)

            for (nx, ny, node, end_dir) in endpoints:
                if x == nx and y == ny:
                    node.add_in_edge(self)
                    self.to_node = node
                    self.to_node_side = self.dir_to_tikz_dir(end_dir)

                    if data[y][x+1] == ':':
                        self.label = data[y][x+2]
                    return
            dir = self.update_dir(data[y][x], dir)
            if dir == None:
                raise Exception('Angle too sharp, or path missing at (%d, %d)' % (x, y))

    def dir_to_tikz_dir(self, dir):
        if dir == 0:
            return 'north'
        elif dir == 1:
            return 'north east'
        elif dir == 2:
            return 'east'
        elif dir == 3:
            return 'south east'
        elif dir == 4:
            return 'south'
        elif dir == 5:
            return 'south west'
        elif dir == 6:
            return 'west'
        elif dir == 7:
            return 'north west'
        else:
            raise Exception('Unknown direction %d' % dir)

    def follow_connection(self, data, x, y, dir):
        elem = data[y][x]
        if elem == ' ':
            raise Exception('Cannot follow path to %d, %d' % (x, y))

        if elem in ARROWS:
            self.head = ARROWS[elem]
        elif elem == ' ':
            raise Exception('Not a path at (%d, %d)' % (elem, x, y))

        if data[y][x+1] == ':':
            self.label = data[y][x+2]

        (nx, ny) = self.walk_in_dir(x, y, dir)
        return (nx, ny, dir)

    # Given a point and direction, figure out the new direction for the given pasth element
    def update_dir(self, elem, dir):
        if elem == '|':
            if dir == 2 or dir == 6:
                return None
            elif dir == 7 or dir < 2:
                return 0
            else:
                return 4
        elif elem == '-':
            if dir == 0 or dir == 4:
                return None
            elif dir < 4:
                return 2
            else:
                return 6
        elif elem == '/':
            if dir == 3 or dir == 7:
                return None
            elif dir < 3:
                return 1
            else:
                return 5
        elif elem == '\\':
            if dir == 1 or dir == 5:
                return None
            elif dir == 0 or dir > 5:
                return 7
            else:
                return 3
        elif not elem == ' ': # Skip over labels, but error on spaces
            return dir
        else:
            return None

    def walk_in_dir(self, x, y, dir):
        if dir == 0:
            d = (0, -1)
        elif dir == 1:
            d = (1, -1)
        elif dir == 2:
            d = (1, 0)
        elif dir == 3:
            d = (1, 1)
        elif dir == 4:
            d = (0, 1)
        elif dir == 5:
            d = (-1, 1)
        elif dir == 6:
            d = (-1, 0)
        elif dir == 7:
            d = (-1, -1)
        else:
            raise Exception('Unknown orientation %d' % dir)
        dx, dy = d
        return (x + dx, y + dy)


    def to_dot(self):
        if self.label == 'refdec':
            return '\\draw[%sedge] \t(%s.%s) \t-- (%s.%s);' % (self.label, self.from_node.id, self.from_node_side, self.to_node.id, self.to_node_side)
        else:
            shift = 'xshift=-1ex'
            return '\\draw[%s] \t(%s.%s) \t-- (%s.%s) \tnode[pos=0.5, %s] {%s};' % (self.label, self.from_node.id, self.from_node_side, self.to_node.id, self.to_node_side, shift, self.label)

class Node (object):
    new_id = 0

    def __init__(self, row, begin, end, content):
        self.in_edge = []
        self.out_edge = []
        self.row = row
        self.begin = begin
        self.end = end

        if content.startswith('(') and content.endswith(')'):
            self.type = 'scope'
        elif content.startswith('[') and content.endswith(']'):
            self.type = 'refdec'
        else:
            raise Exception('Could not determine type of node %s' % content)

        self.content = content[1:-1]
        self.id = '%s_%d' % (self.content[0], Node.new_id)
        Node.new_id += 1

    def add_in_edge(self, edge):
        self.in_edge.append(edge)

    def add_out_edge(self, edge):
        self.out_edge.append(edge)

    def discover_connections(self, data):
        connections = []

        # Top / Bottom
        for x in range(self.begin+1, self.end-1):
            connections.append(self.get_cell(data, x, self.row - 1, 0))
            connections.append(self.get_cell(data, x, self.row + 1, 4))

        # Sides
        connections.append(self.get_cell(data, self.begin - 1, self.row, 6))
        connections.append(self.get_cell(data, self.end, self.row, 2))

        # Corners
        connections.append(self.get_cell(data, self.begin, self.row - 1, 7))
        connections.append(self.get_cell(data, self.begin, self.row + 1, 5))
        connections.append(self.get_cell(data, self.end - 1      , self.row - 1, 1))
        connections.append(self.get_cell(data, self.end - 1      , self.row + 1, 3))


        connections = list(filter((lambda x: not x is None), connections))

        return connections

    # Dir 0 == North, 2 == East, 4 == South
    def get_cell(self, data, x, y, dir):
        if y < 0 or x < 0 or y >= len(data):
            return None
        row = data[y]
        if x >= len(row):
            return None
        val = row[x]
        if val == ' ':
            return None
        elif val in ARROWS or val in EDGES:
            return (x, y, 'o' if val in EDGES else 'i', self, dir)
        else:
            return None

    def to_dot(self):
        x = -(self.end - self.begin) * 2
        y = -self.row / 2

        return '\\node (%s) \t[%s] \tat(%f, %f) {%s};' % (self.id, self.type, x, y, self.content)

EDGES = ['-', '/', '\\', '|']
ARROWS = {
  '>' : '',
  '<' : '',
  '^' : ''
}

def discover_nodes_in_line(row_idx, line):
    res = re.finditer('(\(.*\))|(\[.*\])', line)
    nodes = []
    for match in res:
        nodes.append(Node(row_idx, match.start(), match.end(), match.group()))
    return nodes

def filter_connections(connections):
    out = []
    inn = []
    for (x, y, type, node, dir) in connections:
        if type == 'i':
            inn.append( (x, y, node, dir) )
        elif type == 'o':
            out.append( (x, y, node, dir) )
        else:
            raise Exception('Unknown edge type %s' % type)
    return (inn, out)

def follow_connection(start, ends, data):
    (x, y, node, dir) = start
    connection = Connection(node, x, y, dir, data, ends)
    return connection

def build_header():
    return '''\\begin{figure}[htb]
  \\tikzstyle{refdec}   = [rectangle, minimum width=.6cm, minimum height=.6cm, text centered, draw=black, fill=black!5, font=\\ttfamily]
  \\tikzstyle{scope}    = [ellipse,   minimum width=.9cm, minimum height=.9cm, text centered, draw=black, font=\\ttfamily]

  \\tikzstyle{P}          = [thick,-{Triangle}, font=\\ttfamily]
  \\tikzstyle{refdecedge} = [-{Triangle}, font=\\ttfamily]
  \\tikzstyle{I}          = [-{Triangle[open]}, font=\\ttfamily]

  \\begin{tikzpicture}'''

def build_footer(temp_caption):
    return '  \\end{tikzpicture}\n' + ('' if not temp_caption else '  \\caption{%s}\n' % temp_caption) + '\\end{figure}'

if __name__ == "__main__":
    parser = argparse.ArgumentParser(description='Scope graph to Tikz converter')
    parser.add_argument('file', metavar='F', type=argparse.FileType('r'),
                       help='The file to parse the scopegraph from')

    args = parser.parse_args()
    lines = args.file.readlines()

    if lines[0].startswith('#'):
        caption = lines[0][1:-1]
        lines = lines[1:]
    else:
        caption = None

    nodes = [y for x in  map((lambda x: discover_nodes_in_line(*x)), enumerate(lines)) for y in x]

    chararray = list(map((lambda x : [c for c in x if not c is '\n']), lines))


    # Fill the char array with trailing spaces
    maxlength = max(map((lambda x: len(x)), chararray))
    chararray = list(map((lambda x: x + [' '] * (maxlength - len(x))), chararray))

    connections = []
    for node in nodes:
        new_connections = node.discover_connections(chararray)
        connections.extend(new_connections)
    inn, out = filter_connections(connections)

    connections = []
    for startpoint in out:
        connections.append(follow_connection(startpoint, inn, chararray))

    print('Parsed %d nodes and %d connections' % (len(nodes), len(connections)))
    with open(args.file.name[0:-4] + '.tex', 'w') as file:
        header = build_header()
        file.write(header)
        file.write('\n')

        for node in nodes:
            out = node.to_dot()
            file.write('    ')
            file.write(out)
            file.write('\n')

        file.write('\n\n')

        for connection in connections:
            out = connection.to_dot()
            file.write('    ')
            file.write(out)
            file.write('\n')
        footer = build_footer(caption)
        file.write(footer)
