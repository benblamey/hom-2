// https://stackoverflow.com/questions/105034/how-to-create-a-guid-uuid
function generateUUID() { // Public Domain/MIT
  var d = new Date().getTime();//Timestamp
  var d2 = ((typeof performance !== 'undefined') && performance.now && (performance.now() * 1000)) || 0;//Time in microseconds since page-load or 0 if unsupported
  return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, function (c) {
    var r = Math.random() * 16;//random number between 0 and 16
    if (d > 0) {//Use timestamp until depleted
      r = (d + r) % 16 | 0;
      d = Math.floor(d / 16);
    } else {//Use microseconds since page-load if supported
      r = (d2 + r) % 16 | 0;
      d2 = Math.floor(d2 / 16);
    }
    return (c === 'x' ? r : (r & 0x3 | 0x8)).toString(16);
  });
}

function getSparkCodeForKafkaStream(streamID) {
  return `
  df = spark \\
    .readStream \\
    .format("kafka") \\
    .option("kafka.bootstrap.servers", "host1:port1,host2:port2") \\
    .option("subscribe", "haste_stream_${streamID}") \\
    .load()
  `;
}

function addOutputStreamClicked(node) {
  console.log(node.name)
  node.outputStreams.push(
    {
      id: generateUUID(),
      items_streamed: ko.observable(0),
    }
  )

}

function visit_all(tree, func) {
  for (tierIndex in demo_ih_model.tiers()) {
    child = demo_ih_model.tiers()[tierIndex]
    console.log(`visiting tier ${child}`)
    func(child)
  }
}

function getColor(interestingness) {
  function pad2_hex(number) {
    return (number < 16 ? '0' : '') + number.toString(16)
  }

  r = 0 + Math.round(interestingness * 255);
  g = 0;
  b = 0 + Math.round((1 - interestingness) * 255);
  colstr =  `#${pad2_hex(r)}${pad2_hex(g)}${pad2_hex(b)}`
  console.log(colstr)
  return colstr
}

function initializeNode(node) {
  node.isSelected = ko.observable(false);
  node.childGroupingMethod = ko.observable("radio_child_grouping_method_none");
  node.minInt = ko.observable(0)
  node.maxInt = ko.observable(1)
  console.log(node)
  node.childGroupingMethod.subscribe((newvalue) => childGroupingMethodChanged(node, newvalue));
}

function connectToStreamClick(stream) {
  window.prompt('Copy the code below:', getSparkCodeForKafkaStream(stream.id));
}

function newNode(name) {
  node = {
    name: name,
    id: generateUUID(),
    children: ko.observableArray(),
    data_points: ko.observableArray(),
    outputStreams: ko.observableArray(),
  };
  initializeNode(node);
  return node;
}

function updateInterestingnessOnChildren(node) {
  children = node.children();
  max = node.maxInt();
  min = node.minInt();
  num_of_children = node.children().length;
  for (i = 0; i < num_of_children; i++) {
    children[i].minInt(min + i * (1/num_of_children) * (max - min));
    children[i].maxInt(min + (i + 1) * (1/num_of_children) * (max - min));
  }
}

function updateFrequencies() {
  visit_all(demo_ih_model.root_node, node => {
    node.data_points(demo_ih_model.data_points().filter(p => p.interestingness > node.minInt() && p.interestingness < node.maxInt()));

    node.outputStreams().forEach(stream => {
      stream.items_streamed(  Math.min(node.data_points().length, stream.items_streamed() + Math.floor(Math.random() * 30)) );
    })
  });

}

function childGroupingMethodChanged(node, newValue) {
  console.log(`node ${node} has new child grouping value ${newValue}`)
  switch (newValue) {
    case "radio_child_grouping_method_none":
      node.children.removeAll()
      break;
    case "radio_child_grouping_method_explicit":
      expr = window.prompt(`Choose explicit splitting function:`, "feature.foo > 42");
      node.children.removeAll()
      node.children.push(newNode(`Group B - ${expr} is false`))
      node.children.push(newNode(`Group A - ${expr} is true`))
      break;
    case "radio_child_grouping_method_unsupervised_clustering":
      number_of_clusters = 1*window.prompt(`e.g. How many clusters?:`, 5);
      clustering_alg = window.prompt(`e.g. which algorithm? which feature?:`,'(k-means, feature_foo)');
      node.children.removeAll()
      for (i = 0; i < number_of_clusters; i++) {
        node.children.push(newNode(`${clustering_alg} - cluster ${String.fromCharCode(97 + (i) % 26)}`))
      }
      break;
    case "radio_child_grouping_method_unsupervised_binning":
      feature_to_bin = window.prompt(`bin on which feature?`, 'feature_foo')
      number_of_bins = 1*window.prompt(`e.g. How many quantiles? (e.g. 4,10):`, '4');
      node.children.removeAll()
      for (i = 0; i < number_of_bins; i++) {
        node.children.push(newNode(`quantile ${i} on feature ${feature_to_bin}`))
      }
      break;
    case "radio_child_grouping_method_supervised_al":
      node.children.removeAll()
      node.children.push(newNode("active learning class A"))
      node.children.push(newNode("active learning class B"))
      break;
  }

  updateInterestingnessOnChildren(node)
  visit_all(node, n => updateFrequencies(n))
}

var demo_ih_model = {
  tiers: null,
}

function newDataPoint() {
  return {
    interestingness: Math.random(),
    feature_foo: Math.random() * 100,
    feature_bar: Math.random(),
    feature_wibble: Math.floor(Math.random() * 10),
    example_image: './ex-'+ Math.floor(Math.random() * 10) + '.png',
  }
}



function foo_setInterval() {
  console.log('adding new data items');
  new_data_points_per_interval = 100 + Math.floor(Math.random() * 10);
  for (i = 0; i < new_data_points_per_interval; i++ ){
    demo_ih_model.data_points().push(newDataPoint());

  }
  updateFrequencies();
}

function refreshTiers() {

  document.body.style.cursor = "wait";

  let xhr = new XMLHttpRequest();
  //xhr.open('GET', 'http://localhost:4567/info-fake');
  xhr.open('GET', 'http://localhost:4567/info');
  xhr.send();
  xhr.onload = function() {
    if (xhr.status != 200) {
      alert(`Error ${xhr.status}: ${xhr.statusText}`);
    } else {
      tiers = JSON.parse(xhr.responseText);
      console.log("tiers object arrived");
      console.log(tiers);

      // if (demo_ih_model.tiers == null) {
      //   // new view model
      //   console.log(ko.mapping);
      //   demo_ih_model.tiers = ko.mapping.fromJS(tiers.tiers);
      // } else {
        // update the view model
      newVM = ko.mapping.fromJS(tiers);
      console.log('newVM');
      console.log(newVM);

      demo_ih_model.tiers.removeAll()

      for (tierIndex in newVM.tiers()) {
        tier = newVM.tiers()[tierIndex]
        // fractional color, converted to RGB inside the view.
        tier.color = tierIndex / newVM.tiers().length;
        tier.isSelected = ko.observable(false);
        tier.tierIndex = ko.observable(parseInt(tierIndex));
        tier.sampleJSON = ko.observable("{}");
        demo_ih_model.tiers.push(tier)
      }

      if (demo_ih_model.selectedNode() == null) {
        if (demo_ih_model.tiers().length > 0) {
          onNewNodeSelected(demo_ih_model.tiers()[0])
        }
      }
      if (!demo_ih_model.tiers().includes(demo_ih_model.selectedNode())) {
        if (demo_ih_model.tiers().length == 0) {
          onNewNodeSelected(null);
        } else {
          oldIndex = demo_ih_model.selectedNode().tierIndex()
          if (oldIndex < demo_ih_model.tiers().length) {
            onNewNodeSelected(demo_ih_model.tiers()[oldIndex]);
          } else {
            onNewNodeSelected(demo_ih_model.tiers()[demo_ih_model.tiers().length - 1]);
          }
        }
      }



      //ko.mapping.fromJS(tiers.tiers, demo_ih_model.tiers);

      console.log('demo_ih_model');
      // console.log(demo_ih_model);
      console.log(demo_ih_model.tiers().length);
      console.log(demo_ih_model.tiers());

      document.body.style.cursor = "default";
    }
  }
}

window.onload = (event) => {
  console.log("window.onload")
  demo_ih_model.tiers = ko.observableArray();
  demo_ih_model.selectedNode = ko.observable();
  demo_ih_model.available_functions = ko.observableArray();

  refreshTiers();

  // set demo_ih_model as the root VM.
  // this is referred to as $data from within the bindings.
  ko.applyBindings(demo_ih_model);

  setInterval(refreshTiers, 8000);

  // // Demo data for now
  // demo_ih_model = {
  //   root_node: newNode("root (all data)"),
  //   data_points: ko.observableArray(),
  // }
  // console.log(demo_ih_model)
  //
  // // visit_all(demo_ih_model.root_node, (node) => {
  // //   initializeNode(node)
  // // })
  //

  //demo_ih_model.root_node.isSelected(true);
  //
  // console.log(demo_ih_model)


  // Get the modal
  var modal = document.getElementById("myModal");
// Get the button that opens the modal
  var btn = document.getElementById("myBtn");
// Get the <span> element that closes the modal
  var span = document.getElementsByClassName("close")[0];

// When the user clicks on the button, open the modal
  btn.onclick = function() {

    document.body.style.cursor = "wait";
    let xhr = new XMLHttpRequest();
    xhr.open('GET', 'http://localhost:4567/available_functions');
    xhr.send();
    xhr.onload = function () {
      document.body.style.cursor = "default";

      if (xhr.status != 200) {
        alert(`Error ${xhr.status}: ${xhr.statusText}`);
      } else {
        resp = JSON.parse(xhr.responseText)
        console.log('/available_functions')
        console.log(resp)
        demo_ih_model.available_functions(resp.functions)
        modal.style.display = "block";
      }
    }
  }

  var btnNotebookTierConfirm = document.getElementById("create_tier_from_notebook_confirm_button")
  btnNotebookTierConfirm.onclick = function () {
    document.body.style.cursor = "wait";

    let xhr = new XMLHttpRequest();
    xhr.open('POST', 'http://localhost:4567/create_notebook_tier');
    xhr.send(
      JSON.stringify({
        "notebook_and_function": document.getElementById("notebook_and_function_select").value,
        "shell_cmds": document.getElementById("shell_cmds").value
      })
    );
    xhr.onload = function () {
      document.body.style.cursor = "default";

      if (xhr.status != 200) {
        alert(`Error ${xhr.status}: ${xhr.statusText}`);
      } else {
        resp = JSON.parse(xhr.responseText)
        console.log('/create_notebook_tier')
        console.log(resp)

        // TODO: clear the fields
        modal.style.display = "none";

      }
    }

  }


// When the user clicks on <span> (x), close the modal
  span.onclick = function() {
    modal.style.display = "none";
  }

// When the user clicks anywhere outside of the modal, close it
  window.onclick = function(event) {
    if (event.target == modal) {
      modal.style.display = "none";
    }
  }

};

function getSample(node) {
  console.log("getSample")
  console.log(node)
  document.body.style.cursor = "wait";
  let xhr = new XMLHttpRequest();
  xhr.open('GET', 'http://localhost:4567/sample/' + node.outputTopic());
  xhr.send();
  xhr.onload = function () {
    document.body.style.cursor = "default";

    if (xhr.status != 200) {
      alert(`Error ${xhr.status}: ${xhr.statusText}`);
    } else {
        // make it pretty
      node.sampleJSON(JSON.stringify(JSON.parse(xhr.responseText)));
    }
  }
}

function onNewNodeSelected(node) {
  visit_all(demo_ih_model.root_node, (node) => {
    node.isSelected(false)
  })
  if (node != null) {
    node.isSelected(true);
    demo_ih_model.selectedNode(node)

    getSample(node);
  }
}

function removeTier() {
  document.body.style.cursor = "wait";
  let xhr = new XMLHttpRequest();
  xhr.open('POST', 'http://localhost:4567/remove-tier');
  xhr.send();
  xhr.onload = function () {
    document.body.style.cursor = "default";
    refreshTiers();
  }
}



function addJexlTier() {
  expr = window.prompt(`Choose JEXL expression:`, "data.foo > 42");

  document.body.style.cursor = "wait";
  let xhr = new XMLHttpRequest();
  xhr.open('POST', 'http://localhost:4567/add-tier');
  xhr.send(
    JSON.stringify({
      "jexl_expression": expr
    })
  );
  xhr.onload = function () {
    document.body.style.cursor = "default";
    refreshTiers();
  }
}

treeview_item_clicked = (node) => {
  console.log('treeview_item_clicked');
  console.log(node);
  onNewNodeSelected(node);
}

console.log('imported demo-ih-model.js');